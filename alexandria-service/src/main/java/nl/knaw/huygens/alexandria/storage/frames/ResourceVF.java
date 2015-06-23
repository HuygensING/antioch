package nl.knaw.huygens.alexandria.storage.frames;

import java.util.List;

import nl.knaw.huygens.alexandria.storage.Labels;
import peapod.annotations.Edge;
import peapod.annotations.In;
import peapod.annotations.Vertex;

@Vertex(Labels.RESOURCE)
public abstract class ResourceVF extends AlexandriaVF {
  public abstract String getRef();

  public abstract void setRef(String ref);

  @In
  @Edge(AnnotationVF.ANNOTATES_RESOURCE)
  public abstract List<AnnotationVF> getAnnotatedBy();

}
