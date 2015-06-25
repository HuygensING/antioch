package nl.knaw.huygens.alexandria.storage.frames;

import java.util.List;

import nl.knaw.huygens.alexandria.storage.Labels;
import peapod.annotations.Edge;
import peapod.annotations.In;
import peapod.annotations.Out;
import peapod.annotations.Vertex;

@Vertex(Labels.RESOURCE)
public abstract class SubResourceVF extends AlexandriaVF {
  private static final String PART_OF = "part_of";

  public abstract String getSub();

  public abstract void setSub(String sub);

  @Out
  @Edge(PART_OF)
  public abstract ResourceVF getParentResource();

  @In
  @Edge(AnnotationVF.ANNOTATES_RESOURCE)
  public abstract List<AnnotationVF> getAnnotatedBy();

}
