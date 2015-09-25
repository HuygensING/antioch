package nl.knaw.huygens.alexandria.query;

import static java.util.stream.Collectors.toList;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import com.google.common.collect.ImmutableList;

import nl.knaw.huygens.alexandria.storage.frames.AnnotationVF;

enum QueryField {
  id(AnnotationVF::getUuid), //
  url(AlexandriaQueryParser::getAnnotationURL), //
  when(AnnotationVF::getProvenanceWhen), //
  who(AnnotationVF::getProvenanceWho), //
  why(AnnotationVF::getProvenanceWhy), //
  type(AnnotationVF::getType), //
  value(AnnotationVF::getValue), //
  state(AnnotationVF::getState), //
  resource_id(AnnotationVF::getResourceId), //
  subresource_id(AnnotationVF::getSubResourceId), //
  resource_url(AlexandriaQueryParser::getResourceURL), //
  subresource_url(AlexandriaQueryParser::getSubResourceURL);

  static final ImmutableList<QueryField> RESOURCE_FIELDS = ImmutableList.of(//
      resource_id, resource_url, //
      subresource_id, subresource_url//
  );

  static final List<String> ALL_EXTERNAL_NAMES = Arrays.stream(values())//
      .map(QueryField::externalName)//
      .collect(toList());

  Function<AnnotationVF, Object> getter;

  QueryField(Function<AnnotationVF, Object> getter) {
    this.getter = getter;
  }

  public String externalName() {
    return name().replace('_', '.');
  }

  public static QueryField fromExternalName(String externalName) {
    return valueOf(externalName.replace('.', '_'));
  }

}