package nl.knaw.huygens.alexandria.query;

import java.util.Map;
import java.util.function.Function;

import com.google.common.collect.ImmutableMap;

import nl.knaw.huygens.alexandria.model.search.QueryField;
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
      .build();

  public static Function<AnnotationVF, Object> get(QueryField queryField) {
    return getterMap.get(queryField);
  }
}
