package nl.knaw.huygens.alexandria.query;

import java.util.List;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

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
      paq.setVfClazz(AnnotationVF.class);

    } else if (find.equals("resource")) {
      paq.setVfClazz(ResourceVF.class);

    } else {
      parseErrors.add("unknown type '" + find + "' in find, should be 'annotation' or 'resource'");
    }
  }

  private static void parseWhere(String where, ParsedAlexandriaQuery paq) {
    // TODO: implement!
  }

  private static void parseSort(String sort, ParsedAlexandriaQuery paq) {
    // TODO: implement!
  }

  private static void parseReturn(String fieldString, ParsedAlexandriaQuery paq) {
    List<String> fields = Splitter.on(",").trimResults().splitToList(fieldString);
    paq.setReturnFields(fields);
  }

}
