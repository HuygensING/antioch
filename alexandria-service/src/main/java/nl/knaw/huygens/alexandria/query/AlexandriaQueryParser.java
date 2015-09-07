package nl.knaw.huygens.alexandria.query;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.apache.commons.lang3.StringUtils;
import org.parboiled.common.ImmutableList;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;

import nl.knaw.huygens.Log;
import nl.knaw.huygens.alexandria.antlr.AQLLexer;
import nl.knaw.huygens.alexandria.antlr.AQLParser;
import nl.knaw.huygens.alexandria.endpoint.LocationBuilder;
import nl.knaw.huygens.alexandria.endpoint.search.AlexandriaQuery;
import nl.knaw.huygens.alexandria.model.AlexandriaResource;
import nl.knaw.huygens.alexandria.storage.Storage;
import nl.knaw.huygens.alexandria.storage.frames.AlexandriaVF;
import nl.knaw.huygens.alexandria.storage.frames.AnnotationVF;
import nl.knaw.huygens.alexandria.storage.frames.ResourceVF;

public class AlexandriaQueryParser {
  static final String ALLOWEDFIELDS = ", available fields: " + Joiner.on(", ").join(QueryField.ALL_EXTERNAL_NAMES);

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

    // create a predicate for filtering the list of annotationVFs based on the remaining tokens
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
              .flatMap(rvf -> rvf.getAnnotatedBy().stream());//
          Stream<AnnotationVF> annotationVFStream = Stream.concat(resourceAnnotationsStream, subresourceAnnotationsStream);

          return annotationVFStream;
        }
        // Should return error, since no resource found with given uuid
        return ImmutableList.<AnnotationVF> of().stream();
      };

    }
    return null;
  }

  // @Deprecated
  // private void kludgeForHandlingNLA87(final ParsedAlexandriaQuery paq, final List<WhereToken> tokens) {
  // WhereToken resourceWhereToken = null;
  // List<WhereToken> filteredTokens = Lists.newArrayList();
  // for (WhereToken whereToken : tokens) {
  // // NLA-87
  // if (whereToken.getProperty().equals(QueryField.resource_id)//
  // && whereToken.getFunction().equals(QueryFunction.eq)) {
  // resourceWhereToken = whereToken;
  // } else {
  // filteredTokens.add(whereToken);
  // }
  // }
  //
  // if (resourceWhereToken != null) {
  // String uuid = (String) resourceWhereToken.getParameters().get(0);
  // Function<Storage, List<AnnotationVF>> annotationVFFinder = storage -> {
  // Optional<ResourceVF> optionalResource = storage.readVF(ResourceVF.class, UUID.fromString(uuid));
  // if (optionalResource.isPresent()) {
  // ResourceVF resourceVF = optionalResource.get();
  // Stream<AnnotationVF> resourceAnnotationsStream = resourceVF.getAnnotatedBy().stream();
  // Stream<AnnotationVF> subresourceAnnotationsStream = resourceVF.getSubResources().stream()//
  // .flatMap(rvf -> rvf.getAnnotatedBy().stream());//
  // Stream<AnnotationVF> annotationVFStream = Stream.concat(resourceAnnotationsStream, subresourceAnnotationsStream);
  //
  // // TODO: recurse over annotations to also return annotations (on annotations)+ on resource ?
  // for (WhereToken whereToken : filteredTokens) {
  // annotationVFStream = annotationVFStream.filter(predicate(whereToken));
  // }
  //
  // return annotationVFStream.collect(toList());
  // }
  // // Should return error, since no resource found with given uuid
  // return Lists.newArrayList();
  // };
  // paq.setAnnotationVFFinder(annotationVFFinder);
  // }
  //
  // }
  //
  // @Deprecated
  // private Predicate<AnnotationVF> predicate(WhereToken whereToken) {
  // // who.eq("someone")
  // if (whereToken.getProperty().equals(QueryField.who)//
  // && whereToken.getFunction().equals(QueryFunction.eq)) {
  // String value = (String) whereToken.getParameters().get(0);
  // return (AnnotationVF avf) -> {
  // return avf.getProvenanceWho().equals(value);
  // };
  // }
  // // TODO implement predicate generation
  // return alwaysTrue();
  // }

  private Class<? extends AlexandriaVF> parseFind(final String find) {
    if (find.equals("annotation")) {
      return AnnotationVF.class;

    } else if (find.equals("resource")) {
      parseErrors.add("find: type 'resource' not supported yet");
      return ResourceVF.class;

    } else {
      parseErrors.add("find: unknown type '" + find + "', should be 'annotation'");
      // parseErrors.add("unknown type '" + find + "' in find, should be 'annotation' or 'resource'");
    }
    return null;
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
    Predicate<AnnotationVF> predicate = tokens.stream()//
        .map(AlexandriaQueryParser::toPredicate)//
        .reduce(alwaysTrue(), (p, np) -> p = p.and(np));
    return predicate;
  }

  static Predicate<AnnotationVF> toPredicate(WhereToken whereToken) {
    // eq
    if (QueryFunction.eq.equals(whereToken.getFunction())) {
      Function<AnnotationVF, Object> getter = whereToken.getProperty().getter;
      Object value = whereToken.getParameters().get(0);
      return (AnnotationVF avf) -> {
        return getter.apply(avf).equals(value);
      };
    }
    return alwaysTrue();
  }

  private static Predicate<AnnotationVF> alwaysTrue() {
    return x -> {
      return true;
    };
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
    final List<Function<AnnotationVF, Object>> valueFunctions = Lists.newArrayList();
    for (final SortToken token : sortTokens) {
      final QueryField field = token.getField();
      final Function<AnnotationVF, Object> valueMapper = field.getter;
      valueFunctions.add(valueMapper);
    }
    return (getComparator(sortKeyGenerator(valueFunctions)));
  }

  private List<SortToken> parseSortString(final String sortString) {
    List<String> sortTokenStrings = splitToList(sortString);

    List<String> sortParseErrors = sortTokenStrings.stream()//
        .map(AlexandriaQueryParser::extractExternalName)//
        .filter(externalName -> !QueryField.ALL_EXTERNAL_NAMES.contains(externalName))//
        .map(invalidFieldName -> "sort: unknown field: " + invalidFieldName + ALLOWEDFIELDS)//
        .collect(toList());
    if (!sortParseErrors.isEmpty()) {
      parseErrors.addAll(sortParseErrors);
      return null;
    }

    final List<SortToken> sortFields = sortTokenStrings.stream()//
        .map(AlexandriaQueryParser::sortToken)//
        .collect(toList());
    return sortFields;
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

  @SuppressWarnings("unused")
  private static Comparator<AnnotationVF> getComparator(final List<Function<AnnotationVF, Object>> valueFunctions) {
    final List<Ordering<AnnotationVF>> subOrders = valueFunctions.stream()//
        .map(AlexandriaQueryParser::ordering)//
        .collect(toList());
    Ordering<AnnotationVF> order = subOrders.remove(0);
    for (final Ordering<AnnotationVF> suborder : subOrders) {
      order = order.compound(suborder);
    }
    return order;
  }

  private static Ordering<AnnotationVF> ordering(final Function<AnnotationVF, Object> function) {
    return new Ordering<AnnotationVF>() {
      @SuppressWarnings("unchecked")
      @Override
      public int compare(final AnnotationVF left, final AnnotationVF right) {
        return ((Comparable<Object>) function.apply(left)).compareTo(function.apply(right));
      }
    };
  }

  private static Function<AnnotationVF, String> sortKeyGenerator(final List<Function<AnnotationVF, Object>> valueFunctions) {
    return (final AnnotationVF avf) -> {
      final StringBuilder sb = new StringBuilder();
      for (final Function<AnnotationVF, Object> function : valueFunctions) {
        sb.append(function.apply(avf));
        sb.append("|");
      }
      // Log.debug("sortKey=" + sb);
      return sb.toString();
    };
  }

  private void parseReturn(final ParsedAlexandriaQuery paq, final String fieldString) {
    final List<String> fields = splitToList(fieldString);
    final List<String> allowedFields = QueryField.ALL_EXTERNAL_NAMES;
    final List<String> unknownFields = Lists.newArrayList(fields);
    unknownFields.removeAll(allowedFields);
    if (!unknownFields.isEmpty()) {
      parseErrors.add("return: unknown field(s) " + Joiner.on(", ").join(unknownFields) + ALLOWEDFIELDS);

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

  private static Comparator<AnnotationVF> getComparator(final Function<AnnotationVF, String> sortKeyGenerator) {
    return (a1, a2) -> sortKeyGenerator.apply(a1).compareTo(sortKeyGenerator.apply(a2));
  }

}
