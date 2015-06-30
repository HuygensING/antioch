package nl.knaw.huygens.alexandria.storage.frames;

import java.util.List;

import nl.knaw.huygens.alexandria.storage.Labels;
import peapod.annotations.Edge;
import peapod.annotations.In;
import peapod.annotations.Out;
import peapod.annotations.Vertex;

@Vertex(Labels.SUBRESOURCE)
public abstract class SubResourceVF extends AlexandriaVF {
  static final String PART_OF = "part_of";

  public abstract String getSub();

  public abstract void setSub(String sub);

  @Out
  @Edge(PART_OF)
  public abstract ResourceVF getParentResource();

  @Out
  @Edge(PART_OF)
  public abstract void setParentResource(ResourceVF resourceVF);

  @In
  @Edge(AnnotationVF.ANNOTATES_SUBRESOURCE)
  public abstract List<AnnotationVF> getAnnotatedBy();

}
