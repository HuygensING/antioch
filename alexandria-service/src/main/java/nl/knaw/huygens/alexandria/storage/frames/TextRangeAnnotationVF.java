package nl.knaw.huygens.alexandria.storage.frames;

import nl.knaw.huygens.alexandria.storage.VertexLabels;
import peapod.FramedVertex;
import peapod.annotations.Edge;
import peapod.annotations.In;
import peapod.annotations.Out;
import peapod.annotations.Vertex;

@Vertex(VertexLabels.TEXTRANGEANNOTATION)
public abstract class TextRangeAnnotationVF extends IdentifiableVF implements FramedVertex<TextRangeAnnotationVF> {
  public static class EdgeLabels {
    public static final String HAS_RESOURCE = "textrangeannotation_has_resource";
    public static final String HAS_TEXTANNOTATION = "has_textannotation";
    public static final String ANNOTATES_TEXTRANGE_ANNOTATION = "annotates_textrange_annotation";
    public static final String DEPRECATES = "deprecates";
  }

  public abstract void setRevision(Integer revision);

  public abstract Integer getRevision();

  public abstract String getName();

  public abstract void setName(String name);

  public abstract String getAnnotatorCode();

  public abstract void setAnnotatorCode(String annotatorCode);

  // Position elements: begin

  public abstract String getTargetAnnotationId();

  public abstract void setTargetAnnotationId(String xmlId);

  public abstract String getXmlId();

  public abstract void setXmlId(String xmlId);

  public abstract Integer getOffset();

  public abstract void setOffset(Integer offset);

  public abstract Integer getLength();

  public abstract void setLength(Integer length);

  public abstract String getAbsoluteXmlId();

  public abstract void setAbsoluteXmlId(String xmlId);

  public abstract Integer getAbsoluteOffset();

  public abstract void setAbsoluteOffset(Integer offset);

  public abstract Integer getAbsoluteLength();

  public abstract void setAbsoluteLength(Integer length);

  public abstract void setUseOffset(Boolean useOffset);

  public abstract Boolean getUseOffset();

  // Position elements: end

  public abstract String getAttributesAsJson();

  public abstract void setAttributesAsJson(String json);

  @Out
  @Edge(EdgeLabels.HAS_RESOURCE)
  public abstract void setResource(ResourceVF resource);

  @Out
  @Edge(EdgeLabels.HAS_RESOURCE)
  public abstract ResourceVF getResource();

  @Out
  @Edge(EdgeLabels.ANNOTATES_TEXTRANGE_ANNOTATION)
  public abstract void setTargetTextRangeAnnotation(TextRangeAnnotationVF targetTextRangeAnnotation);

  @Out
  @Edge(EdgeLabels.ANNOTATES_TEXTRANGE_ANNOTATION)
  public abstract TextRangeAnnotationVF getTargetTextRangeAnnotation();

  @Out
  @Edge(EdgeLabels.DEPRECATES)
  public abstract void setDeprecatedAnnotation(TextRangeAnnotationVF annotationToDeprecate);

  @Out
  @Edge(EdgeLabels.DEPRECATES)
  public abstract TextRangeAnnotationVF getDeprecatedAnnotation();

  @In
  @Edge(EdgeLabels.DEPRECATES)
  public abstract TextRangeAnnotationVF getDeprecatedBy();

}
