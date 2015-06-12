package nl.knaw.huygens.alexandria.storage.frames;

import nl.knaw.huygens.alexandria.service.Labels;
import peapod.annotations.Vertex;

@Vertex(Labels.RESOURCE)
public abstract class ResourceVF extends AlexandriaVF {
  public abstract String getRef();

  public abstract void setRef(String ref);

}
