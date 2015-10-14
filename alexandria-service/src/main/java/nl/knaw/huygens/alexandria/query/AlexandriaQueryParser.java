package nl.knaw.huygens.alexandria.query;

/*
 * #%L
 * alexandria-service
 * =======
 * Copyright (C) 2015 Huygens ING (KNAW)
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

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;

import nl.knaw.huygens.Log;
import nl.knaw.huygens.alexandria.antlr.AQLLexer;
import nl.knaw.huygens.alexandria.antlr.AQLParser;
import nl.knaw.huygens.alexandria.endpoint.LocationBuilder;
import nl.knaw.huygens.alexandria.endpoint.search.AlexandriaQuery;
import nl.knaw.huygens.alexandria.model.AlexandriaAnnotation;
import nl.knaw.huygens.alexandria.model.AlexandriaResource;
import nl.knaw.huygens.alexandria.storage.Storage;
import nl.knaw.huygens.alexandria.storage.frames.AlexandriaVF;
import nl.knaw.huygens.alexandria.storage.frames.AnnotationVF;
import nl.knaw.huygens.alexandria.storage.frames.ResourceVF;

public class AlexandriaQueryParser {
  static final String ALLOWED_FIELDS = ", available fields: " + Joiner.on(", ").join(QueryField.ALL_EXTERNAL_NAMES);

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

    if (!parseErrors.isEmpty()) {
      throw new AlexandriaQueryParseException(parseErrors);
    }

    return paq;
  }

  private void setFilter(final ParsedAlexandriaQuery paq, String where) {
    final List<WhereToken> tokens = tokenize(where);

    // any tokens with resource.id or subresource.id need to be filtered out and lead to an annotationVFFinder
    List<WhereToken> resourceWhereTokens = filterResourceWhereTokens(tokens);
    if (!resourceWhereTokens.isEmpty()) {
      paq.setAnnotationVFFinder(createAnnotationVFFinder(resourceWhereTokens));
    }

    tokens.removeAll(resourceWhereTokens);

    // create a predicate for filtering the annotationVF stream based on the remaining tokens
    paq.setPredicate(createPredicate(tokens));
  }

  private List<WhereToken> filterResourceWhereTokens(List<WhereToken> tokens) {
    return tokens.stream()//
        .filter(WhereToken::hasResourceProperty)//
        .collect(toList());
  }

  private Function<Storage, Stream<AnnotationVF>> createAnnotationVFFinder(List<WhereToken> resourceWhereTokens) {
    WhereToken resourceWhereToken = resourceWhereTokens.get(0);
    if (resourceWhereTokens.size() == 1 && resourceWhereToken.getFunction().equals(QueryFunction.eq)) {
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

    }
    return null;
  }

  private Class<? extends AlexandriaVF> parseFind(final String find) {
    switch (find) {
    case "annotation":
      return AnnotationVF.class;

    case "resource":
      parseErrors.add("find: type 'resource' not supported yet");
      return ResourceVF.class;

    default:
      parseErrors.add("find: unknown type '" + find + "', should be 'annotation'");
      // parseErrors.add("unknown type '" + find + "' in find, should be 'annotation' or 'resource'");
      return null;
    }
  }

  List<WhereToken> tokenize(String whereString) {
    Log.info("whereString=<{}>", whereString);
    if (StringUtils.isEmpty(whereString)) {
      // parseErrors.add("empty or missing where");
      return Lists.newArrayList();
    }

    QueryErrorListener errorListener = new QueryErrorListener();
    CharStream stream = new ANTLRInputStream(whereString);
    AQLLexer lex = new AQLLexer(stream);
    lex.removeErrorListeners();
    lex.addErrorListener(errorListener);
    CommonTokenStream tokenStream = new CommonTokenStream(lex);
    AQLParser parser = new AQLParser(tokenStream);
    parser.removeErrorListeners();
    parser.addErrorListener(errorListener);
    parser.setBuildParseTree(true);
    ParseTree tree = parser.root();
    Log.info("tree={}", tree.toStringTree(parser));
    if (errorListener.heardErrors()) {
      parseErrors.addAll(errorListener.getParseErrors());
      return Lists.newArrayList();
    }

    QueryVisitor visitor = new QueryVisitor();
    visitor.visit(tree);
    parseErrors.addAll(errorListener.getParseErrors());
    return visitor.getWhereTokens();
  }

  private static Predicate<AnnotationVF> createPredicate(List<WhereToken> tokens) {
    if (tokens.isEmpty()) {
      return alwaysTrue();
    }

    return tokens.stream()//
        .map(AlexandriaQueryParser::toPredicate)//
        .reduce(alwaysTrue(), Predicate::and);
  }

  static Predicate<AnnotationVF> toPredicate(WhereToken whereToken) {
    Function<AnnotationVF, Object> getter = whereToken.getProperty().getter;
    // eq
    if (QueryFunction.eq.equals(whereToken.getFunction())) {
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
        if (propertyValue instanceof Long) {
          return ((Long) propertyValue).compareTo((Long) lowerLimit) >= 0//
              && ((Long) propertyValue).compareTo((Long) upperLimit) <= 0;

        }
        return true;
      };
    }

    return alwaysTrue();
  }

  private static Predicate<AnnotationVF> alwaysTrue() {
    return x -> true;
  }

  static String getAnnotationURL(final AnnotationVF avf) {
    return locationBuilder.locationOf(AlexandriaAnnotation.class, avf.getUuid()).toString();
  }

  static String getResourceURL(final AnnotationVF avf) {
    return id2url(avf.getResourceId());
  }

  static String getSubResourceURL(final AnnotationVF avf) {
    return id2url(avf.getSubResourceId());
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
    Function<AnnotationVF, Object> function = token.getField().getter;
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
    final List<String> allowedFields = QueryField.ALL_EXTERNAL_NAMES;
    final List<String> unknownFields = Lists.newArrayList(fields);
    unknownFields.removeAll(allowedFields);
    if (!unknownFields.isEmpty()) {
      parseErrors.add("return: unknown field(s) " + Joiner.on(", ").join(unknownFields) + ALLOWED_FIELDS);

    } else {
      paq.setReturnFields(fields);

      final Function<AnnotationVF, Map<String, Object>> mapper = avf -> fields.stream()//
          .collect(toMap(Function.identity(), f -> QueryField.fromExternalName(f).getter.apply(avf)));
      // TODO: cache resultmapper?
      paq.setResultMapper(mapper);
    }
  }

  private static List<String> splitToList(final String fieldString) {
    return Splitter.on(",").trimResults().splitToList(fieldString);
  }

}
