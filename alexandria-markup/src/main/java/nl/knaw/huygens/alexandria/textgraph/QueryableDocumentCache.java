package nl.knaw.huygens.alexandria.textgraph;

/*
 * #%L
 * alexandria-main
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
