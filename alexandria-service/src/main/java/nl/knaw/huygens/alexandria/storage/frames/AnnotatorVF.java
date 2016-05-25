package nl.knaw.huygens.alexandria.storage.frames;

import nl.knaw.huygens.alexandria.storage.VertexLabels;
import peapod.FramedVertex;
import peapod.annotations.Edge;
import peapod.annotations.Out;
import peapod.annotations.Vertex;

@Vertex(VertexLabels.ANNOTATOR)
public abstract class AnnotatorVF implements VF, FramedVertex<AnnotatorVF> {
  public static final String HAS_RESOURCE = "annotator_has_resource";

  public abstract void setCode(String code);

  public abstract String getCode();

  public abstract void setDescription(String description);

  public abstract String getDescription();

  @Out
  @Edge(HAS_RESOURCE)
  public abstract void setResource(ResourceVF resource);

  @Out
  @Edge(HAS_RESOURCE)
  public abstract ResourceVF getResource();

}
