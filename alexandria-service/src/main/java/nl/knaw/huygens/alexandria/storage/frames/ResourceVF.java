package nl.knaw.huygens.alexandria.storage.frames;

import java.util.List;

import nl.knaw.huygens.alexandria.storage.Labels;
import peapod.annotations.Edge;
import peapod.annotations.In;
import peapod.annotations.Out;
import peapod.annotations.Vertex;

@Vertex(Labels.RESOURCE)
public abstract class ResourceVF extends AlexandriaVF {
  static final String PART_OF = "part_of";

  public abstract String getCargo();

  public abstract void setCargo(String cargo);

  @In
  @Edge(AnnotationVF.ANNOTATES_RESOURCE)
  public abstract List<AnnotationVF> getAnnotatedBy();

  @In
  @Edge(PART_OF)
  public abstract List<ResourceVF> getSubResources();

  @Out
  @Edge(PART_OF)
  public abstract ResourceVF getParentResource();

  @Out
  @Edge(PART_OF)
  public abstract void setParentResource(ResourceVF resourceVF);

  public boolean isSubresource() {
    return getParentResource() != null;
  }

}
