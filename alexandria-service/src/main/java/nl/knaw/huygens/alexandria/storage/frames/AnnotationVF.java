package nl.knaw.huygens.alexandria.storage.frames;

import nl.knaw.huygens.alexandria.service.Labels;
import peapod.annotations.Edge;
import peapod.annotations.Vertex;

@Vertex(Labels.ANNOTATION)
public abstract class AnnotationVF extends AlexandriaVF {
  private static final String ANNOTATES = "annotates";

  public abstract void setType(String type);

  public abstract String getType();

  public abstract void setValue(String value);

  public abstract String getValue();

  @Edge(ANNOTATES)
  public abstract AnnotationVF getAnnotatedAnnotation();

  @Edge(ANNOTATES)
  public abstract void setAnnotatedAnnotation(AnnotationVF annotationToAnnotate);

  @Edge(ANNOTATES)
  public abstract ResourceVF getAnnotatedResource();

  @Edge(ANNOTATES)
  public abstract void setAnnotatedResource(ResourceVF resourceToAnnotate);
}
