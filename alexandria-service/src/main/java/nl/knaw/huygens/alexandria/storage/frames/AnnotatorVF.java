package nl.knaw.huygens.alexandria.storage.frames;

import nl.knaw.huygens.alexandria.storage.VertexLabels;
import peapod.FramedVertex;
import peapod.annotations.Vertex;

@Vertex(VertexLabels.ANNOTATOR)
public abstract class AnnotatorVF extends HasResourceVF implements FramedVertex<AnnotatorVF> {

  public abstract void setCode(String code);

  public abstract String getCode();

  public abstract void setDescription(String description);

  public abstract String getDescription();


}
