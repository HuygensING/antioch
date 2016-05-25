package nl.knaw.huygens.alexandria.storage.frames;

import peapod.FramedVertex;

public abstract class TextRangeAnnotationVF extends HasResourceVF implements FramedVertex<TextRangeAnnotationVF> {

  public abstract String getId();

  public abstract void setId(String id);

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

}
