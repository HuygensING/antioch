package nl.knaw.huygens.alexandria.query;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
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
import org.apache.tinkerpop.gremlin.process.traversal.Traverser;
import org.parboiled.common.Predicates;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;

import nl.knaw.huygens.Log;
import nl.knaw.huygens.alexandria.antlr.AQLLexer;
import nl.knaw.huygens.alexandria.antlr.AQLParser;
import nl.knaw.huygens.alexandria.endpoint.LocationBuilder;
import nl.knaw.huygens.alexandria.endpoint.search.AlexandriaQuery;
import nl.knaw.huygens.alexandria.model.AlexandriaResource;
import nl.knaw.huygens.alexandria.storage.frames.AlexandriaVF;
import nl.knaw.huygens.alexandria.storage.frames.AnnotationVF;
import nl.knaw.huygens.alexandria.storage.frames.ResourceVF;

public class AlexandriaQueryParser {

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

    parseReturn(query.getFields(), paq);

    if (!parseErrors.isEmpty()) {
      throw new AlexandriaQueryParseException(parseErrors);
    }

    return paq;
  }

  private void setFilter(final ParsedAlexandriaQuery paq, String where) {
    final List<WhereToken> tokens = tokenize(where);

    kludgeForHandlingNLA87(paq, tokens);

    paq.setPredicate(createPredicate(tokens));
  }

  private void kludgeForHandlingNLA87(final ParsedAlexandriaQuery paq, final List<WhereToken> tokens) {
    WhereToken resourceWhereToken = null;
    List<WhereToken> filteredTokens = Lists.newArrayList();
    for (WhereToken whereToken : tokens) {
      // NLA-87
      if (whereToken.getProperty().equals("resource.id")//
          && whereToken.getFunction().equals(MatchFunction.eq)) {
        resourceWhereToken = whereToken;
      } else {
        filteredTokens.add(whereToken);
      }
    }

    if (resourceWhereToken != null) {
      String uuid = (String) resourceWhereToken.getParameters().get(0);
      paq.setAnnotationVFFinder(storage -> {
        Optional<ResourceVF> optionalResource = storage.readVF(ResourceVF.class, UUID.fromString(uuid));
        if (optionalResource.isPresent()) {
          ResourceVF resourceVF = optionalResource.get();
          Stream<AnnotationVF> resourceAnnotationsStream = resourceVF.getAnnotatedBy().stream();
          Stream<AnnotationVF> subresourceAnnotationsStream = resourceVF.getSubResources().stream()//
              .flatMap(rvf -> rvf.getAnnotatedBy().stream());//
          Stream<AnnotationVF> annotationVFStream = Stream.concat(resourceAnnotationsStream, subresourceAnnotationsStream);

          // TODO: recurse over annotations to also return annotations (on annotations)+ on resource ?
          for (WhereToken whereToken : filteredTokens) {
            annotationVFStream = annotationVFStream.filter(predicate(whereToken));
          }

          return annotationVFStream.collect(toList());
        }
        // Should return error, since no resource found with given uuid
        return Lists.newArrayList();
      });
    }

  }

  private Predicate<AnnotationVF> predicate(WhereToken whereToken) {
    // who.eq("someone")
    if (whereToken.getProperty().equals("who")//
        && whereToken.getFunction().equals(MatchFunction.eq)) {
      String value = (String) whereToken.getParameters().get(0);
      return (AnnotationVF avf) -> {
        return avf.getProvenanceWho().equals(value);
      };
    }
    // TODO implement predicate generation
    return (Predicate<AnnotationVF>) Predicates.alwaysTrue();
  }

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

  static final Map<String, Function<AnnotationVF, Object>> valueMapping = ImmutableMap.<String, Function<AnnotationVF, Object>> builder()//
      .put("id", AnnotationVF::getUuid)//
      .put("when", AnnotationVF::getProvenanceWhen)//
      .put("who", AnnotationVF::getProvenanceWho)//
      .put("why", AnnotationVF::getProvenanceWhy)//
      .put("type", AnnotationVF::getType)//
      .put("value", AnnotationVF::getValue)//
      .put("resource.id", AnnotationVF::getResourceId)//
      .put("subresource.id", AnnotationVF::getSubResourceId)//
      .put("resource.url", AlexandriaQueryParser::getResourceURL)//
      .put("subresource.url", AlexandriaQueryParser::getSubResourceURL)//
      .build();

  static final String ALLOWEDFIELDS = ", available fields: " + Joiner.on(", ").join(valueMapping.keySet());

  // static final Pattern P1 = Pattern.compile("([a-z\\.]+)\\.([a-z]+)\\((.*)\\)");
  //
  // static List<WhereToken> tokenize(final String whereString) {
  // Log.info("whereString=<{}>", whereString);
  // List<String> strings = splitToList(whereString);
  // List<WhereToken> list = Lists.newArrayListWithExpectedSize(strings.size());
  // for (String string : strings) {
  // Log.info("part=<{}>", string);
  // Matcher matcher = P1.matcher(string);
  // if (!matcher.matches()) {
  // parseErrors.add("unparsable part in where: '" + string + "'");
  //
  // } else {
  // WhereToken token = new WhereToken();
  // String property = matcher.group(1);
  // Log.info("property=<{}>", property);
  // String functionString = matcher.group(2);
  // Log.info("function=<{}>", functionString);
  // String parameterString = matcher.group(3);
  // Log.info("parameterString=<{}>", parameterString);
  // try {
  // MatchFunction function = MatchFunction.valueOf(functionString);
  // List<Object> parameters = Splitter.on(",").splitToList(parameterString).stream()//
  // .map(AlexandriaQueryParser::parseParameter)//
  // .collect(toList());
  //
  // token.setProperty(property);
  // token.setFunction(function);
  // token.setParameters(parameters);
  // list.add(token);
  // } catch (IllegalArgumentException e) {
  // parseErrors.add("invalid part in where: unknown function " + functionString);
  // }
  // }
  // }
  // return list;
  // }

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

  private static Predicate<Traverser<AnnotationVF>> createPredicate(List<WhereToken> tokens) {
    if (tokens.isEmpty()) {
      return alwaysTrue();
    }
    return annotationVFTraverser -> {
      boolean pass = true;
      final AnnotationVF annotationVF = annotationVFTraverser.get();
      for (WhereToken token : tokens) {
        pass = pass && assertMatch(annotationVF, token);
      }
      return pass;
    };
  }

  private static Predicate<Traverser<AnnotationVF>> alwaysTrue() {
    return x -> {
      return true;
    };
  }

  private static final WhereToken STATE_IS_CONFIRMED = new WhereToken()//
      .setProperty("state")//
      .setFunction(MatchFunction.eq)//
      .setParameters(ImmutableList.of("CONFIRMED"));

  private static boolean assertMatch(AnnotationVF annotationVF, WhereToken token) {
    if (STATE_IS_CONFIRMED.equals(token)) {
      return annotationVF.isConfirmed();
    }
    return true;
  }

  private static String getResourceURL(final AnnotationVF avf) {
    return id2url(avf.getResourceId());
  }

  private static String id2url(String resourceId) {
    if (StringUtils.isNotEmpty(resourceId) && !AnnotationVF.NO_VALUE.equals(resourceId)) {
      return locationBuilder.locationOf(AlexandriaResource.class, resourceId).toString();
    }
    return ":null";
  }

  private static String getSubResourceURL(final AnnotationVF avf) {
    return id2url(avf.getSubResourceId());
  }

  private Comparator<AnnotationVF> parseSort(final String sortString) {
    // TODO: cache resultcomparator?
    boolean errorInSort = false;
    final List<SortToken> sortTokens = parseSortString(sortString);
    final List<Function<AnnotationVF, Object>> valueFunctions = Lists.newArrayList();
    for (final SortToken token : sortTokens) {
      final String field = token.getField();
      final Function<AnnotationVF, Object> valueMapper = valueMapping.get(field);
      if (valueMapper == null) {
        parseErrors.add("sort: unknown field: " + field + ALLOWEDFIELDS);
        errorInSort = true;
      } else {
        valueFunctions.add(valueMapper);
      }
    }
    if (!errorInSort) {
      return (getComparator(sortKeyGenerator(valueFunctions)));
      // return getComparator(valueFunctions);
    }
    return null;
  }

  private static List<SortToken> parseSortString(final String sortString) {
    final List<SortToken> sortFields = splitToList(sortString).stream()//
        .map(AlexandriaQueryParser::sortToken)//
        .collect(toList());
    return sortFields;
  }

  static SortToken sortToken(final String f) {
    return new SortToken()//
        .setAscending(!f.startsWith("-"))//
        .setField(f.replaceFirst("^[\\-\\+]", ""));
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

  private void parseReturn(final String fieldString, final ParsedAlexandriaQuery paq) {
    final List<String> fields = splitToList(fieldString);
    final Set<String> allowedFields = valueMapping.keySet();
    final List<String> unknownFields = Lists.newArrayList(fields);
    unknownFields.removeAll(allowedFields);
    if (!unknownFields.isEmpty()) {
      parseErrors.add("return: unknown field(s) " + Joiner.on(", ").join(unknownFields) + ALLOWEDFIELDS);

    } else {
      paq.setReturnFields(fields);

      final Function<AnnotationVF, Map<String, Object>> mapper = avf -> fields.stream()//
          .collect(toMap(Function.identity(), f -> valueMapping.get(f).apply(avf)));
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

  static class SortToken {
    private String field = "";
    private boolean ascending = true;

    public String getField() {
      return field;
    }

    public SortToken setField(final String field) {
      this.field = field;
      return this;
    }

    public boolean isAscending() {
      return ascending;
    }

    public SortToken setAscending(final boolean ascending) {
      this.ascending = ascending;
      return this;
    }
  }

  enum MatchFunction {
    eq, match, inSet, inRange
  }

  static class WhereToken {
    String property;
    MatchFunction function;
    List<Object> parameters = Lists.newArrayList();

    public String getProperty() {
      return property;
    }

    public WhereToken setProperty(String property) {
      this.property = property;
      return this;
    }

    public MatchFunction getFunction() {
      return function;
    }

    public WhereToken setFunction(MatchFunction function) {
      this.function = function;
      return this;
    }

    public List<Object> getParameters() {
      return parameters;
    }

    public WhereToken setParameters(List<Object> parameters) {
      this.parameters = parameters;
      return this;
    }

  }
}
