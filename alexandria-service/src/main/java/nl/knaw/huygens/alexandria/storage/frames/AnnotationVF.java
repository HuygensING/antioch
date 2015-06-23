package nl.knaw.huygens.alexandria.storage.frames;

import java.util.List;

import nl.knaw.huygens.alexandria.storage.Labels;
import peapod.annotations.Edge;
import peapod.annotations.In;
import peapod.annotations.Vertex;

@Vertex(Labels.ANNOTATION)
public abstract class AnnotationVF extends AlexandriaVF {
  // TODO: double-check if peapod supports outgoing edges with the same label to different types of VF
  static final String ANNOTATES_RESOURCE = "annotates_resource";
  static final String ANNOTATES_ANNOTATION = "annotates_annotation";
  private static final String HAS_BODY = "has_body";

  @Edge(ANNOTATES_ANNOTATION)
  public abstract AnnotationVF getAnnotatedAnnotation();

  @Edge(ANNOTATES_ANNOTATION)
  public abstract void setAnnotatedAnnotation(AnnotationVF annotationToAnnotate);

  @Edge(ANNOTATES_RESOURCE)
  public abstract ResourceVF getAnnotatedResource();

  @Edge(ANNOTATES_RESOURCE)
  public abstract void setAnnotatedResource(ResourceVF resourceToAnnotate);

  @Edge(HAS_BODY)
  public abstract AnnotationBodyVF getBody();

  @Edge(HAS_BODY)
  public abstract void setBody(AnnotationBodyVF body);

  @In
  @Edge(AnnotationVF.ANNOTATES_ANNOTATION)
  public abstract List<AnnotationVF> getAnnotatedBy();

}
