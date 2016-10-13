package nl.knaw.huygens.alexandria.storage.frames;

import nl.knaw.huygens.alexandria.storage.VertexLabels;
import peapod.FramedVertex;
import peapod.annotations.Edge;
import peapod.annotations.Out;
import peapod.annotations.Vertex;

@Vertex(VertexLabels.TEXTRANGEANNOTATION)
public abstract class TextRangeAnnotationVF extends IdentifiableVF implements FramedVertex<TextRangeAnnotationVF> {
  public static class EdgeLabels {
    public static final String HAS_RESOURCE = "textrangeannotation_has_resource";
    public static final String HAS_TEXTANNOTATION = "has_textannotation";
  }

  public abstract String getName();

  public abstract void setName(String name);

  public abstract String getAnnotatorCode();

  public abstract void setAnnotatorCode(String annotatorCode);

  public abstract String getXmlId();

  public abstract void setXmlId(String xmlId);

  public abstract Integer getOffset();

  public abstract void setOffset(Integer offset);

  public abstract Integer getLength();

  public abstract void setLength(Integer length);

  public abstract String getAttributesAsJson();

  public abstract void setAttributesAsJson(String json);

  @Out
  @Edge(EdgeLabels.HAS_RESOURCE)
  public abstract void setResource(ResourceVF resource);

  @Out
  @Edge(EdgeLabels.HAS_RESOURCE)
  public abstract ResourceVF getResource();

}
