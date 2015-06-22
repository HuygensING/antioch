package nl.knaw.huygens.alexandria.storage.frames;

import nl.knaw.huygens.alexandria.storage.Labels;
import peapod.annotations.Edge;
import peapod.annotations.Vertex;

@Vertex(Labels.ANNOTATION)
public abstract class AnnotationVF extends AlexandriaVF {
  static final String ANNOTATES = "annotates";
  private static final String HAS_BODY = "has_body";

  @Edge(ANNOTATES)
  public abstract AnnotationVF getAnnotatedAnnotation();

  @Edge(ANNOTATES)
  public abstract void setAnnotatedAnnotation(AnnotationVF annotationToAnnotate);

  @Edge(ANNOTATES)
  public abstract ResourceVF getAnnotatedResource();

  @Edge(ANNOTATES)
  public abstract void setAnnotatedResource(ResourceVF resourceToAnnotate);

  @Edge(HAS_BODY)
  public abstract AnnotationBodyVF getBody();

  @Edge(HAS_BODY)
  public abstract void setBody(AnnotationBodyVF body);
}
