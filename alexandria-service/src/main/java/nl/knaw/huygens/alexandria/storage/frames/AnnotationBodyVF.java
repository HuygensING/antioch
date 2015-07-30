package nl.knaw.huygens.alexandria.storage.frames;

import java.util.List;

import nl.knaw.huygens.alexandria.storage.Labels;
import peapod.annotations.Edge;
import peapod.annotations.In;
import peapod.annotations.Vertex;

@Vertex(Labels.ANNOTATIONBODY)
public abstract class AnnotationBodyVF extends AlexandriaVF {
  public abstract void setType(String type);

  public abstract String getType();

  public abstract void setValue(String value);

  public abstract String getValue();

  @In
  @Edge(AnnotationVF.HAS_BODY)
  public abstract List<AnnotationVF> getOfAnnotationList();

}
