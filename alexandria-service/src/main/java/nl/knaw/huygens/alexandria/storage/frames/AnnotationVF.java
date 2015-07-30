package nl.knaw.huygens.alexandria.storage.frames;

import java.util.List;

import nl.knaw.huygens.alexandria.storage.Labels;
import peapod.annotations.Edge;
import peapod.annotations.In;
import peapod.annotations.Out;
import peapod.annotations.Vertex;

@Vertex(Labels.ANNOTATION)
public abstract class AnnotationVF extends AlexandriaVF {
  // TODO: double-check if (update of) peapod supports outgoing edges with the same label to different types of VF
  static final String ANNOTATES_RESOURCE = "annotates_resource";
  private static final String ANNOTATES_ANNOTATION = "annotates_annotation";
  private static final String DEPRECATES = "deprecates";
  static final String HAS_BODY = "has_body";

  @Out
  @Edge(HAS_BODY)
  public abstract AnnotationBodyVF getBody();

  @Out
  @Edge(HAS_BODY)
  public abstract void setBody(AnnotationBodyVF body);

  @Out
  @Edge(ANNOTATES_ANNOTATION)
  public abstract AnnotationVF getAnnotatedAnnotation();

  @Out
  @Edge(ANNOTATES_ANNOTATION)
  public abstract void setAnnotatedAnnotation(AnnotationVF annotationToAnnotate);

  @Out
  @Edge(ANNOTATES_RESOURCE)
  public abstract ResourceVF getAnnotatedResource();

  @Out
  @Edge(ANNOTATES_RESOURCE)
  public abstract void setAnnotatedResource(ResourceVF resourceToAnnotate);

  @In
  @Edge(AnnotationVF.ANNOTATES_ANNOTATION)
  public abstract List<AnnotationVF> getAnnotatedBy();

  @Out
  @Edge(AnnotationVF.DEPRECATES)
  public abstract void setDeprecatedAnnotation(AnnotationVF annotationToDeprecate);

  @Out
  @Edge(AnnotationVF.DEPRECATES)
  public abstract AnnotationVF getDeprecatedAnnotation();

  @In
  @Edge(AnnotationVF.DEPRECATES)
  public abstract AnnotationVF getDeprecatedBy();

}
