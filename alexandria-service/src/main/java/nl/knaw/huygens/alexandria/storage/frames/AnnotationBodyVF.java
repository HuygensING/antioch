package nl.knaw.huygens.alexandria.storage.frames;

import nl.knaw.huygens.alexandria.storage.Labels;
import peapod.annotations.Vertex;

@Vertex(Labels.ANNOTATIONBODY)
public abstract class AnnotationBodyVF extends AlexandriaVF {
  public abstract void setType(String type);

  public abstract String getType();

  public abstract void setValue(String value);

  public abstract String getValue();
}
