package nl.knaw.huygens.alexandria.query;

/*
 * #%L
 * alexandria-service
 * =======
 * Copyright (C) 2015 - 2017 Huygens ING (KNAW)
 * =======
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.apache.commons.lang3.StringUtils;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;

import nl.knaw.huygens.alexandria.antlr.AQLLexer;
import nl.knaw.huygens.alexandria.antlr.AQLParser;
import nl.knaw.huygens.alexandria.antlr.QueryErrorListener;
import nl.knaw.huygens.alexandria.api.model.AlexandriaState;
import nl.knaw.huygens.alexandria.api.model.search.AlexandriaQuery;
import nl.knaw.huygens.alexandria.api.model.search.QueryField;
import nl.knaw.huygens.alexandria.api.model.search.QueryFunction;
import nl.knaw.huygens.alexandria.endpoint.LocationBuilder;
import nl.knaw.huygens.alexandria.exception.BadRequestException;
import nl.knaw.huygens.alexandria.model.AlexandriaAnnotation;
import nl.knaw.huygens.alexandria.model.AlexandriaResource;
import nl.knaw.huygens.alexandria.storage.Storage;
import nl.knaw.huygens.alexandria.storage.frames.AlexandriaVF;
import nl.knaw.huygens.alexandria.storage.frames.AnnotationVF;
import nl.knaw.huygens.alexandria.storage.frames.ResourceVF;
import nl.knaw.huygens.alexandria.util.StreamUtil;

public class AlexandriaQueryParser {
  static final String ALLOWED_FIELDS = ", available fields: " + Joiner.on(", ").join(QueryField.ALL_EXTERNAL_NAMES);
  static final String ALLOWED_FUNCTIONS = ", available functions: " + Joiner.on(", ").join(QueryFunction.values());

  private static LocationBuilder locationBuilder;

  List<String> parseErrors = Lists.newArrayList();

  @Inject
  public AlexandriaQueryParser(final LocationBuilder locationBuilder) {
    AlexandriaQueryParser.locationBuilder = locationBuilder;
  }

  public ParsedAlexandriaQuery parse(final AlexandriaQuery query) {
    parseErrors.clear();

    final ParsedAlexandriaQuery paq = new ParsedAlexandriaQuery()//
        .setVFClass(parseFind(query.getFind()))//
        .setResultComparator(parseSort(query.getSort()));

    setFilter(paq, query.getWhere());

    parseReturn(paq, query.getFields());

    paq.setDistinct(query.isDistinct());

    if (!parseErrors.isEmpty()) {
      throw new AlexandriaQueryParseException(parseErrors);
    }

    return paq;
  }

  private void setFilter(final ParsedAlexandriaQuery paq, String where) {
    final List<WhereToken> tokens = tokenize(where);

    // add default stateToken unless there is a state clause in the where
    addDefaultStateTokenWhenNeeded(tokens);

    // any tokens with resource.id or subresource.id need to be filtered out and lead to an annotationVFFinder
    List<WhereToken> resourceWhereTokens = filterResourceWhereTokens(tokens);
    tokens.removeAll(resourceWhereTokens);
    if (ResourceVF.class == paq.getVFClass()) {
      paq.setResultStreamMapper(createResultStreamMapper(resourceWhereTokens));

    } else {
      if (!resourceWhereTokens.isEmpty()) {
        Function<Storage, Stream<AnnotationVF>> annotationVFFinder = createAnnotationVFFinder(resourceWhereTokens);
        if (annotationVFFinder != null) {
          paq.setAnnotationVFFinder(annotationVFFinder);
        }
      }

      // create a predicate for filtering the annotationVF stream based on the remaining tokens
      paq.setPredicate(createPredicate(tokens));
    }
  }

  private Function<Storage, Stream<Map<String, Object>>> createResultStreamMapper(List<WhereToken> resourceWhereTokens) {
    // TODO extend use of resource queries, current implementation only for implementation of nla-264 case
    return (storage) -> {
      GraphTraversal<Vertex, Vertex> traversal = storage.getResourceVertexTraversal();

      Optional<String> rootResourceUUID = resourceWhereTokens.stream()//
          .filter(t -> t.getProperty().equals(QueryField.resource_id)//
              && t.getFunction().equals(QueryFunction.eq))//
          .map(t -> t.getParameters().get(0))//
          .map(String.class::cast)//
          .findFirst();
      if (rootResourceUUID.isPresent()) {
        traversal = traversal.has(Storage.IDENTIFIER_PROPERTY, rootResourceUUID.get());
      }

      Optional<String> sub = resourceWhereTokens.stream()//
          .filter(t -> t.getProperty().equals(QueryField.subresource_sub)//
              && t.getFunction().equals(QueryFunction.eq))//
          .map(t -> t.getParameters().get(0))//
          .map(String.class::cast)//
          .findFirst();
      if (sub.isPresent()) {
        traversal = traversal//
            .until(__.has(ResourceVF.Properties.CARGO, sub.get()))//
            .repeat(__.in(ResourceVF.EdgeLabels.PART_OF));
      }

      return StreamUtil.stream(traversal)//
          .map(v -> storage.frameVertex(v, ResourceVF.class))//
          .map(this::toResultMap);
    };
  }

  private Map<String, Object> toResultMap(ResourceVF rvf) {
    Map<String, Object> map = new HashMap<>();
    map.put(QueryField.subresource_id.externalName(), rvf.getUuid());
    // map.put(QueryField.subresource_sub.externalName(), rvf.getCargo());
    return map;
  }

  private void addDefaultStateTokenWhenNeeded(List<WhereToken> tokens) {
    boolean addStateToken = tokens.stream()//
        .noneMatch(token -> QueryField.state.equals(token.getProperty()));
    if (addStateToken) {
      WhereToken defaultStateToken = new WhereToken(//
          QueryField.state, //
          QueryFunction.eq, //
          ImmutableList.of(AlexandriaState.CONFIRMED.name())//
      );
      tokens.add(defaultStateToken);
    }
  }

  private List<WhereToken> filterResourceWhereTokens(List<WhereToken> tokens) {
    return tokens.stream()//
        .filter(WhereToken::hasResourceProperty)//
        .collect(toList());
  }

  private Function<Storage, Stream<AnnotationVF>> createAnnotationVFFinder(List<WhereToken> resourceWhereTokens) {
    // TODo: refactor
    WhereToken resourceWhereToken = resourceWhereTokens.get(0);
    if (resourceWhereTokens.size() == 1 && resourceWhereToken.getFunction().equals(QueryFunction.eq) && resourceWhereToken.getProperty().equals(QueryField.resource_id)) {
      String uuid = (String) resourceWhereToken.getParameters().get(0);
      return storage -> {
        Optional<ResourceVF> optionalResource = storage.readVF(ResourceVF.class, UUID.fromString(uuid));
        if (optionalResource.isPresent()) {
          ResourceVF resourceVF = optionalResource.get();
          Stream<AnnotationVF> resourceAnnotationsStream = resourceVF.getAnnotatedBy().stream();
          Stream<AnnotationVF> subresourceAnnotationsStream = resourceVF.getSubResources().stream()//
              .flatMap(rvf -> rvf.getAnnotatedBy().stream());
          return Stream.concat(resourceAnnotationsStream, subresourceAnnotationsStream);
        }
        // Should return error, since no resource found with given uuid
        return ImmutableList.<AnnotationVF> of().stream();
      };

    } else if (resourceWhereTokens.size() == 1 && resourceWhereToken.getFunction().equals(QueryFunction.eq) && resourceWhereToken.getProperty().equals(QueryField.resource_ref)) {
      return storage -> {
        Object cargo = resourceWhereToken.getParameters().get(0);
        // Log.info("cargo={}", cargo);
        List<ResourceVF> resourceVFs = storage.find(ResourceVF.class)//
            .has(ResourceVF.Properties.CARGO, cargo)//
            .toList();

        List<UUID> resourceUUIDs = resourceVFs.stream()//
            .map(ResourceVF::getUuid)//
            .map(UUID::fromString)//
            .collect(toList());
        // Log.info("resourceUUIDs={}", resourceUUIDs);
        return toAnnotationVFStream(resourceUUIDs, storage);
      };

    } else if (resourceWhereToken.getFunction().equals(QueryFunction.inSet)) {
      List<UUID> uuidSet = resourceWhereToken.getParameters().stream()//
          .map(String.class::cast)//
          .map(UUID::fromString)//
          .collect(toList());
      return storage -> toAnnotationVFStream(uuidSet, storage);

    }
    return null;
  }

  private Stream<AnnotationVF> toAnnotationVFStream(List<UUID> uuidSet, Storage storage) {
    List<AnnotationVF> annotationList = new ArrayList<>();
    for (UUID uuid : uuidSet) {
      Optional<ResourceVF> optionalResource = storage.readVF(ResourceVF.class, uuid);
      optionalResource.ifPresent(resourceVF -> {
        annotationList.addAll(resourceVF.getAnnotatedBy());
        annotationList.addAll(resourceVF.getSubResources().stream()//
            .map(ResourceVF::getAnnotatedBy)//
            .flatMap(Collection::stream)//
            .collect(toList()));
      });
    }
    // Log.info("annotationList={}", annotationList);
    return annotationList.stream();
  }

  private Class<? extends AlexandriaVF> parseFind(final String find) {
    switch (find) {
    case "annotation":
      return AnnotationVF.class;

    case "resource":
      // parseErrors.add("find: type 'resource' not supported yet");
      return ResourceVF.class;

    default:
      parseErrors.add("find: unknown type '" + find + "', should be 'annotation' or 'resource'");
      return null;
    }
  }

  List<WhereToken> tokenize(String whereString) {
    // Log.info("whereString=<{}>", whereString);
    if (StringUtils.isEmpty(whereString)) {
      // parseErrors.add("empty or missing where");
      return Lists.newArrayList();
    }

    QueryErrorListener errorListener = new QueryErrorListener();
    CharStream stream = new ANTLRInputStream(whereString);
    AQLLexer lex = new AQLLexer(stream);
    lex.removeErrorListeners();
    CommonTokenStream tokenStream = new CommonTokenStream(lex);
    AQLParser parser = new AQLParser(tokenStream);
    parser.removeErrorListeners();
    parser.addErrorListener(errorListener);
    parser.setBuildParseTree(true);
    ParseTree tree = parser.root();
    // Log.info("tree={}", tree.toStringTree(parser));
    if (errorListener.heardErrors()) {
      parseErrors.addAll(errorListener.getParseErrors().stream()//
          .map(AlexandriaQueryParser::clarifyParseError)//
          .collect(toList()));
      return Lists.newArrayList();
    }

    QueryVisitor visitor = new QueryVisitor();
    visitor.visit(tree);
    parseErrors.addAll(errorListener.getParseErrors());
    return visitor.getWhereTokens();
  }

  private static final String MISSING_FIELD_NAME = "missing FIELD_NAME";
  private static final String MISSING_FUNCTION = "missing FUNCTION";

  private static String clarifyParseError(String parseError) {
    if (parseError.contains(MISSING_FIELD_NAME)) {
      return parseError.replace(MISSING_FIELD_NAME, "missing or invalid field") + ALLOWED_FIELDS;
    }
    if (parseError.contains(MISSING_FUNCTION)) {
      return parseError.replace(MISSING_FUNCTION, "missing or invalid function") + ALLOWED_FUNCTIONS;
    }
    return parseError;
  }

  private static Predicate<AnnotationVF> createPredicate(List<WhereToken> tokens) {
    if (tokens.isEmpty()) {
      return alwaysTrue();
    }

    return tokens.stream()//
        .map(AlexandriaQueryParser::toPredicate)//
        .reduce(alwaysTrue(), Predicate::and);
  }

  static Set<String> ALL_STATES = Arrays.stream(AlexandriaState.values()).map(AlexandriaState::name).collect(toSet());

  static Predicate<AnnotationVF> toPredicate(WhereToken whereToken) {
    Function<AnnotationVF, Object> getter = QueryFieldGetters.get(whereToken.getProperty());
    // eq
    if (QueryFunction.eq.equals(whereToken.getFunction())) {
      checkForValidStateParameter(whereToken);
      Object eqValue = whereToken.getParameters().get(0);
      return avf -> getter.apply(avf).equals(eqValue);
    }

    // match
    if (QueryFunction.match.equals(whereToken.getFunction())) {
      // TODO: catch errors
      String matchValue = (String) whereToken.getParameters().get(0);
      Pattern p = Pattern.compile(matchValue);
      return (AnnotationVF avf) -> {
        String propertyValue = (String) getter.apply(avf);
        Matcher matcher = p.matcher(propertyValue);
        return matcher.matches();
      };
    }

    // inSet
    if (QueryFunction.inSet.equals(whereToken.getFunction())) {
      checkForValidStateParameter(whereToken);
      List<Object> possibleValues = whereToken.getParameters();
      return (AnnotationVF avf) -> {
        Object propertyValue = getter.apply(avf);
        return possibleValues.contains(propertyValue);
      };
    }

    // inRange
    if (QueryFunction.inRange.equals(whereToken.getFunction())) {
      List<Object> rangeLimits = whereToken.getParameters();
      Object lowerLimit = rangeLimits.get(0);
      Object upperLimit = rangeLimits.get(1);
      return (AnnotationVF avf) -> {
        Object propertyValue = getter.apply(avf);
        if (propertyValue instanceof String) {
          return ((String) propertyValue).compareTo((String) lowerLimit) >= 0//
                  && ((String) propertyValue).compareTo((String) upperLimit) <= 0;
        }
        //
        return !(propertyValue instanceof Long) || ((Long) propertyValue).compareTo((Long) lowerLimit) >= 0//
                && ((Long) propertyValue).compareTo((Long) upperLimit) <= 0;
      };
    }

    return alwaysTrue();
  }

  static final Predicate<String> INVALID_STATEVALUE_PREDICATE = stateValue -> !(stateValue instanceof String && ALL_STATES.contains(stateValue));

  private static void checkForValidStateParameter(WhereToken whereToken) {
    if (QueryField.state.equals(whereToken.getProperty())) {
      List<String> invalidValues = whereToken.getParameters().stream()//
          .map(String.class::cast).filter(INVALID_STATEVALUE_PREDICATE)//
          .collect(toList());
      if (!invalidValues.isEmpty()) {
        String message = ((invalidValues.size() == 1)//
            ? invalidValues.get(0) + " is not a valid value"//
            : Joiner.on(", ").join(invalidValues) + " are not valid values")//
            + " for " + QueryField.state.externalName();
        throw new BadRequestException(message);
      }
    }
  }

  private static Predicate<AnnotationVF> alwaysTrue() {
    return x -> true;
  }

  static String getAnnotationURL(final AnnotationVF avf) {
    return locationBuilder.locationOf(AlexandriaAnnotation.class, avf.getUuid()).toString();
  }

  static String getAnnotationId(final AnnotationVF avf) {
    // for deprecated annotations, remove the revision from the id.
    return avf.getUuid().replaceFirst("\\..*$", "");
  }

  static String getResourceURL(final AnnotationVF avf) {
    return id2url(avf.getResourceId());
  }

  static String getResourceRef(final AnnotationVF avf) {
    return avf.getResource().getCargo();
  }

  static String getSubResourceURL(final AnnotationVF avf) {
    return id2url(avf.getSubResourceId());
  }

  static String getSubResourceSub(final AnnotationVF avf) {
    ResourceVF subResource = avf.getSubResource();
    return (subResource != null) ? subResource.getCargo() : ":null";
  }

  private static String id2url(String resourceId) {
    if (StringUtils.isNotEmpty(resourceId) && !AnnotationVF.NO_VALUE.equals(resourceId)) {
      return locationBuilder.locationOf(AlexandriaResource.class, resourceId).toString();
    }
    return ":null";
  }

  private Comparator<AnnotationVF> parseSort(final String sortString) {
    // TODO: cache resultcomparator?
    final List<SortToken> sortTokens = parseSortString(sortString);
    if (sortTokens == null) {
      // there were parse errors
      return null;
    }
    List<Ordering<AnnotationVF>> orderings = sortTokens.stream()//
        .map(AlexandriaQueryParser::ordering)//
        .collect(toList());
    Ordering<AnnotationVF> order = orderings.remove(0);
    for (final Ordering<AnnotationVF> suborder : orderings) {
      order = order.compound(suborder);
    }
    return order;
  }

  private static Ordering<AnnotationVF> ordering(SortToken token) {
    boolean ascending = token.isAscending();
    Function<AnnotationVF, Object> function = QueryFieldGetters.get(token.getField());
    return new Ordering<AnnotationVF>() {
      @SuppressWarnings("unchecked")
      @Override
      public int compare(final AnnotationVF left, final AnnotationVF right) {
        return ascending//
            ? ((Comparable<Object>) function.apply(left)).compareTo(function.apply(right))//
            : ((Comparable<Object>) function.apply(right)).compareTo(function.apply(left));
      }
    };
  }

  private List<SortToken> parseSortString(final String sortString) {
    List<String> sortTokenStrings = splitToList(sortString);

    List<String> sortParseErrors = sortTokenStrings.stream()//
        .map(AlexandriaQueryParser::extractExternalName)//
        .filter(externalName -> !QueryField.ALL_EXTERNAL_NAMES.contains(externalName))//
        .map(invalidFieldName -> "sort: unknown field: " + invalidFieldName + ALLOWED_FIELDS)//
        .collect(toList());
    if (!sortParseErrors.isEmpty()) {
      parseErrors.addAll(sortParseErrors);
      return null;
    }

    return sortTokenStrings.stream()//
        .map(AlexandriaQueryParser::sortToken)//
        .collect(toList());
  }

  static SortToken sortToken(final String f) {
    boolean ascending = !f.startsWith("-");
    String externalName = extractExternalName(f);
    return new SortToken()//
        .setAscending(ascending)//
        .setField(QueryField.fromExternalName(externalName));
  }

  private static String extractExternalName(final String sortParameter) {
    return sortParameter.replaceFirst("^[\\-\\+]", "");
  }

  private void parseReturn(final ParsedAlexandriaQuery paq, final String fieldString) {
    final List<String> fields = splitToList(fieldString);
    final List<String> listFields = extractListfields(fieldString);
    final List<String> allowedFields = QueryField.ALL_EXTERNAL_NAMES;
    final List<String> unknownFields = Lists.newArrayList(fields);
    unknownFields.removeAll(allowedFields);
    if (!unknownFields.isEmpty()) {
      parseErrors.add("return: unknown field(s) " + Joiner.on(", ").join(unknownFields) + ALLOWED_FIELDS);

    } else {
      paq.setReturnFields(fields);
      paq.setFieldsToGroup(listFields);

      final Function<AnnotationVF, Map<String, Object>> mapper = avf -> fields.stream()//
          .collect(toMap(Function.identity(), f -> QueryFieldGetters.get(QueryField.fromExternalName(f)).apply(avf)));
      // TODO: cache resultmapper?
      paq.setResultMapper(mapper);
    }
  }

  static final Pattern LISTPATTERN = Pattern.compile("list\\((.*)\\)");

  private List<String> extractListfields(String fieldString) {
    Matcher matcher = LISTPATTERN.matcher(fieldString);
    String listFields = matcher.find() ? matcher.group(1) : "";
    return splitToList(listFields);
  }

  private static List<String> splitToList(final String fieldString) {
    return Splitter.on(",")//
        .trimResults()//
        .omitEmptyStrings()//
        .splitToList(fieldString.replace("list(", "").replace(")", ""));
  }

}
