package nl.knaw.huygens.antioch.query;

/*
 * #%L
 * antioch-service
 * =======
 * Copyright (C) 2015 - 2017 Huygens ING (KNAW)
 * =======
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.util.Map;
import java.util.function.Function;

import com.google.common.collect.ImmutableMap;

import nl.knaw.huygens.antioch.api.model.search.QueryField;
import nl.knaw.huygens.antioch.storage.frames.AnnotationVF;

class QueryFieldGetters {
  private static final Map<QueryField, Function<AnnotationVF, Object>> getterMap = ImmutableMap.<QueryField, Function<AnnotationVF, Object>> builder()//
      .put(QueryField.id, AntiochQueryParser::getAnnotationId) //
      .put(QueryField.url, AntiochQueryParser::getAnnotationURL) //
      .put(QueryField.when, AnnotationVF::getProvenanceWhen) //
      .put(QueryField.who, AnnotationVF::getProvenanceWho) //
      .put(QueryField.why, AnnotationVF::getProvenanceWhy) //
      .put(QueryField.type, AnnotationVF::getType) //
      .put(QueryField.value, AnnotationVF::getValue) //
      .put(QueryField.state, AnnotationVF::getState) //
      .put(QueryField.resource_id, AnnotationVF::getResourceId) //
      .put(QueryField.subresource_id, AnnotationVF::getSubResourceId) //
      .put(QueryField.resource_url, AntiochQueryParser::getResourceURL) //
      .put(QueryField.subresource_url, AntiochQueryParser::getSubResourceURL)//
      .put(QueryField.resource_ref, AntiochQueryParser::getResourceRef) //
      .put(QueryField.subresource_sub, AntiochQueryParser::getSubResourceSub)//
      .build();

  public static Function<AnnotationVF, Object> get(QueryField queryField) {
    return getterMap.get(queryField);
  }
}
