package nl.knaw.huygens.alexandria.textgraph;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import nl.knaw.huygens.tei.QueryableDocument;

public class QueryableDocumentCache {
  static String AMPERSAND_PLACEHOLDER = "☢";

  static Cache<String, QueryableDocument> cache = CacheBuilder.newBuilder()//
      .maximumSize(10)//
      .build();

  public static QueryableDocument get(String xml) {
    try {
      return cache.get(xml, createQueryableDocument(xml));

    } catch (ExecutionException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

  private static Callable<? extends QueryableDocument> createQueryableDocument(String xml) {
    return () -> {
      String processedXml = xml.replace("&", AMPERSAND_PLACEHOLDER);
      QueryableDocument qDocument = QueryableDocument.createFromXml(processedXml, true);
      cache.put(xml, qDocument);
      return qDocument;
    };
  }

}
