package nl.knaw.huygens.alexandria.query;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

import org.apache.tinkerpop.gremlin.process.traversal.Traverser;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;

import nl.knaw.huygens.Log;
import nl.knaw.huygens.alexandria.endpoint.search.AlexandriaQuery;
import nl.knaw.huygens.alexandria.storage.frames.AlexandriaVF;
import nl.knaw.huygens.alexandria.storage.frames.AnnotationVF;
import nl.knaw.huygens.alexandria.storage.frames.ResourceVF;

public class AlexandriaQueryParser {

  static List<String> parseErrors = Lists.newArrayList();

  public static ParsedAlexandriaQuery parse(AlexandriaQuery query) {
    parseErrors.clear();

    ParsedAlexandriaQuery paq = new ParsedAlexandriaQuery();
    paq.setVFClass(parseFind(query.getFind()));
    paq.setPredicate(parseWhere(query.getWhere()));
    paq.setResultComparator(parseSort(query.getSort()));
    parseReturn(query.getFields(), paq);

    if (!parseErrors.isEmpty()) {
      throw new AlexandriaQueryParseException(parseErrors);
    }

    return paq;
  }

  private static Class<? extends AlexandriaVF> parseFind(String find) {
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

  static final Map<String, Function<AnnotationVF, Object>> valueMapping = ImmutableMap.<String, Function<AnnotationVF, Object>> builder()
      .put("id", AnnotationVF::getUuid)
      .put("when", AnnotationVF::getProvenanceWhen)
      .put("who", AnnotationVF::getProvenanceWho)
      .put("why", AnnotationVF::getProvenanceWhy)
      .put("type", AnnotationVF::getType)
      .put("value", AnnotationVF::getValue)
      .put("resource.id", AnnotationVF::getResourceId)
      .put("subresource.id", AnnotationVF::getSubResourceId)
      .build();

  static final String ALLOWEDFIELDS = ", available fields: " + Joiner.on(", ")
      .join(valueMapping.keySet());

  private static Predicate<Traverser<AnnotationVF>> parseWhere(String whereString) {
    // TODO: implement!
    List<String> tokens = Splitter.on(",")
        .trimResults()
        .splitToList(whereString);
    Predicate<Traverser<AnnotationVF>> predicate = at -> {
      Object y = at.get();
      Log.debug("y={}", y.getClass());
      AnnotationVF x = at.get();
      Log.debug("{}", x);
      return x.isConfirmed();
    };
    return predicate;
  }

  private static Comparator<AnnotationVF> parseSort(String sortString) {
    // TODO: cache resultcomparator?
    boolean errorInSort = false;
    List<SortToken> sortTokens = parseSortString(sortString);
    List<Function<AnnotationVF, Object>> valueFunctions = Lists.newArrayList();
    for (SortToken token : sortTokens) {
      String field = token.getField();
      Function<AnnotationVF, Object> valueMapper = valueMapping.get(field);
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

  private static List<SortToken> parseSortString(String sortString) {
    List<SortToken> sortFields = Splitter.on(",")
        .trimResults()
        .splitToList(sortString)
        .stream()
        .map(f -> sortToken(f))
        .collect(toList());
    return sortFields;
  }

  static SortToken sortToken(String f) {
    SortToken token = new SortToken();
    token.setAscending(!f.startsWith("-"));
    token.setField(f.replaceFirst("^[\\-\\+]", ""));
    return token;
  }

  private static Comparator<AnnotationVF> getComparator(List<Function<AnnotationVF, Object>> valueFunctions) {
    // valueFunctions.stream().map(f -> new )
    // convert to Order<AnnotationVF> functions, return compound
    List<Ordering<AnnotationVF>> subOrders = Lists.newArrayList();
    for (Function<AnnotationVF, Object> function : valueFunctions) {
      subOrders.add(ordering(function));
    }
    Ordering<AnnotationVF> order = subOrders.remove(0);
    for (Ordering<AnnotationVF> suborder : subOrders) {
      order = order.compound(suborder);
    }
    return order;
  }

  private static Ordering<AnnotationVF> ordering(Function<AnnotationVF, Object> function) {
    return new Ordering<AnnotationVF>() {
      @Override
      public int compare(AnnotationVF left, AnnotationVF right) {
        return ((Comparable) function.apply(left)).compareTo(function.apply(right));
      }
    };
  }

  private static Function<AnnotationVF, String> sortKeyGenerator(List<Function<AnnotationVF, Object>> valueFunctions) {
    return (AnnotationVF avf) -> {
      StringBuilder sb = new StringBuilder();
      for (Function<AnnotationVF, Object> function : valueFunctions) {
        sb.append(function.apply(avf));
        sb.append("|");
      }
      Log.debug("sortKey=" + sb);
      return sb.toString();
    };
  }

  private static void parseReturn(String fieldString, ParsedAlexandriaQuery paq) {
    List<String> fields = Splitter.on(",")
        .trimResults()
        .splitToList(fieldString);
    Set<String> allowedFields = valueMapping.keySet();
    List<String> unknownFields = Lists.newArrayList(fields);
    unknownFields.removeAll(allowedFields);
    if (!unknownFields.isEmpty()) {
      parseErrors.add("return: unknown field(s) " + Joiner.on(", ")
          .join(unknownFields) + ALLOWEDFIELDS);

    } else {
      paq.setReturnFields(fields);

      Function<AnnotationVF, Map<String, Object>> mapper = avf -> fields.stream()
          .collect(toMap(Function.identity(), f -> valueMapping.get(f)
              .apply(avf)));
      // TODO: cache resultmapper?
      paq.setResultMapper(mapper);
    }
  }

  private static Comparator<AnnotationVF> getComparator(Function<AnnotationVF, String> sortKeyGenerator) {
    return (a1, a2) -> sortKeyGenerator.apply(a1)
        .compareTo(sortKeyGenerator.apply(a2));
  }

  public static class SortToken {
    private String field = "";
    private boolean ascending = true;

    public String getField() {
      return field;
    }

    public void setField(String field) {
      this.field = field;
    }

    public boolean isAscending() {
      return ascending;
    }

    public void setAscending(boolean ascending) {
      this.ascending = ascending;
    }
  }
}
