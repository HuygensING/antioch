package nl.knaw.huygens.alexandria.query;

/*
 * #%L
 * alexandria-service
 * =======
 * Copyright (C) 2015 - 2016 Huygens ING (KNAW)
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

import java.util.Map;
import java.util.function.Function;

import com.google.common.collect.ImmutableMap;

import nl.knaw.huygens.alexandria.api.model.search.QueryField;
import nl.knaw.huygens.alexandria.storage.frames.AnnotationVF;

public class QueryFieldGetters {
  static final Map<QueryField, Function<AnnotationVF, Object>> getterMap = ImmutableMap.<QueryField, Function<AnnotationVF, Object>> builder()//
      .put(QueryField.id, AlexandriaQueryParser::getAnnotationId) //
      .put(QueryField.url, AlexandriaQueryParser::getAnnotationURL) //
      .put(QueryField.when, AnnotationVF::getProvenanceWhen) //
      .put(QueryField.who, AnnotationVF::getProvenanceWho) //
      .put(QueryField.why, AnnotationVF::getProvenanceWhy) //
      .put(QueryField.type, AnnotationVF::getType) //
      .put(QueryField.value, AnnotationVF::getValue) //
      .put(QueryField.state, AnnotationVF::getState) //
      .put(QueryField.resource_id, AnnotationVF::getResourceId) //
      .put(QueryField.subresource_id, AnnotationVF::getSubResourceId) //
      .put(QueryField.resource_url, AlexandriaQueryParser::getResourceURL) //
      .put(QueryField.subresource_url, AlexandriaQueryParser::getSubResourceURL)//
      .put(QueryField.resource_ref, AlexandriaQueryParser::getResourceRef) //
      .put(QueryField.subresource_sub, AlexandriaQueryParser::getSubResourceSub)//
      .build();

  public static Function<AnnotationVF, Object> get(QueryField queryField) {
    return getterMap.get(queryField);
  }
}
