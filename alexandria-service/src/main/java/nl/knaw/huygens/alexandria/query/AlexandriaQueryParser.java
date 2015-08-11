package nl.knaw.huygens.alexandria.query;

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

import nl.knaw.huygens.Log;
import nl.knaw.huygens.alexandria.endpoint.search.AlexandriaQuery;
import nl.knaw.huygens.alexandria.storage.frames.AnnotationVF;
import nl.knaw.huygens.alexandria.storage.frames.ResourceVF;

public class AlexandriaQueryParser {

  static List<String> parseErrors = Lists.newArrayList();

  public static ParsedAlexandriaQuery parse(AlexandriaQuery query) {
    parseErrors.clear();
    ParsedAlexandriaQuery paq = new ParsedAlexandriaQuery();

    parseFind(query.getFind(), paq);
    parseWhere(query.getWhere(), paq);
    parseSort(query.getSort(), paq);
    parseReturn(query.getFields(), paq);

    if (!parseErrors.isEmpty()) {
      throw new AlexandriaQueryParseException(parseErrors);
    }

    return paq;
  }

  private static void parseFind(String find, ParsedAlexandriaQuery paq) {
    if (find.equals("annotation")) {
      paq.setVFClass(AnnotationVF.class);

    } else if (find.equals("resource")) {
      paq.setVFClass(ResourceVF.class);
      parseErrors.add("find: type 'resource' not supported yet");

    } else {
      parseErrors.add("find: unknown type '" + find + "', should be 'annotation'");
      // parseErrors.add("unknown type '" + find + "' in find, should be 'annotation' or 'resource'");
    }
  }

  private static void parseWhere(String where, ParsedAlexandriaQuery paq) {
    // TODO: implement!
    Predicate<Traverser<AnnotationVF>> predicate = null;
    paq.setPredicate(predicate);
  }

  private static void parseSort(String sortString, ParsedAlexandriaQuery paq) {
    List<String> sortFields = Splitter.on(",").trimResults().splitToList(sortString);
    List<Function<AnnotationVF, Object>> valueFunctions = Lists.newArrayList();
    for (String field : sortFields) {
      Function<AnnotationVF, Object> valueMapper = valueMapping.get(field);
      if (valueMapper == null) {
        parseErrors.add("sort: unknown field: " + field);
      } else {
        valueFunctions.add(valueMapper);
      }
    }
    paq.setResultComparator(getComparator(sortKeyGenerator(valueFunctions)));
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
    List<String> fields = Splitter.on(",").trimResults().splitToList(fieldString);
    Set<String> allowedFields = valueMapping.keySet();
    List<String> unknownFields = Lists.newArrayList(fields);
    unknownFields.removeAll(allowedFields);
    if (!unknownFields.isEmpty()) {
      parseErrors.add("return: unknown field(s) " + Joiner.on(", ").join(unknownFields));
    }
    paq.setReturnFields(fields);

    Function<AnnotationVF, Map<String, Object>> mapper = avf -> fields.stream()//
        .collect(toMap(f -> f, f -> valueMapping.get(f).apply(avf)));
    paq.setResultMapper(mapper);
  }

  static Map<String, Function<AnnotationVF, Object>> valueMapping = ImmutableMap.<String, Function<AnnotationVF, Object>> builder()//
      .put("id", AnnotationVF::getUuid)//
      .put("when", AnnotationVF::getProvenanceWhen)//
      .put("who", AnnotationVF::getProvenanceWho)//
      .put("why", AnnotationVF::getProvenanceWhy)//
      .put("type", AnnotationVF::getType)//
      .put("value", AnnotationVF::getValue)//
      .put("resource.id", AnnotationVF::getResourceId)//
      .put("subresource.id", AnnotationVF::getSubResourceId)//
      .build();

  private static Comparator<AnnotationVF> getComparator(Function<AnnotationVF, String> sortKeyGenerator) {
    return (a1, a2) -> sortKeyGenerator.apply(a1).compareTo(sortKeyGenerator.apply(a2));
  }
}
