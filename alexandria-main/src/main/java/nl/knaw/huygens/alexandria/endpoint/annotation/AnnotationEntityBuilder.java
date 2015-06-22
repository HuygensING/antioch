package nl.knaw.huygens.alexandria.endpoint.annotation;

import javax.inject.Inject;

import nl.knaw.huygens.Log;
import nl.knaw.huygens.alexandria.endpoint.LocationBuilder;
import nl.knaw.huygens.alexandria.model.AlexandriaAnnotation;

public class AnnotationEntityBuilder {

  private LocationBuilder locationBuilder;

  @Inject
  public AnnotationEntityBuilder(LocationBuilder locationBuilder) {
    Log.trace("AnnotationEntityBuilder created: locationBuilder=[{}]", locationBuilder);
    this.locationBuilder = locationBuilder;
  }

  public AnnotationEntity build(AlexandriaAnnotation annotation) {
    return AnnotationEntity.of(annotation).withLocationBuilder(locationBuilder);
  }

}
