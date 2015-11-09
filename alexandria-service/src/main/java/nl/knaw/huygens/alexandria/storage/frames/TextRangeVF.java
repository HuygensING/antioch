package nl.knaw.huygens.alexandria.storage.frames;

import nl.knaw.huygens.alexandria.storage.Labels;
import peapod.annotations.Edge;
import peapod.annotations.Out;
import peapod.annotations.Vertex;

@Vertex(Labels.TEXTRANGE)
public abstract class TextRangeVF extends AlexandriaVF {
  private static final String START_TEXTNODE = "starts_with";
  private static final String END_TEXTNODE = "ends_with";

  @Out
  @Edge(START_TEXTNODE)
  public abstract void setStartTextNode(TextNodeVF startTextNode);

  @Out
  @Edge(START_TEXTNODE)
  public abstract TextNodeVF getStartTextNode();

  @Out
  @Edge(END_TEXTNODE)
  public abstract void setEndTextNode(TextNodeVF endTextNode);

  @Out
  @Edge(END_TEXTNODE)
  public abstract TextNodeVF getEndTextNode();

}
