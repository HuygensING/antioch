package nl.knaw.huygens.alexandria.storage.frames;

import nl.knaw.huygens.alexandria.storage.VertexLabels;
import peapod.annotations.Edge;
import peapod.annotations.Out;
import peapod.annotations.Vertex;

@Vertex(VertexLabels.ANNOTATOR)
public abstract class AnnotatorVF extends VF {

  public static final String HAS_RESOURCE = "has_resource";

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
