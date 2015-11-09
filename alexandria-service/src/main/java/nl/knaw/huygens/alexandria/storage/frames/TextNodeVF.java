package nl.knaw.huygens.alexandria.storage.frames;

import nl.knaw.huygens.alexandria.storage.Labels;
import peapod.annotations.Edge;
import peapod.annotations.In;
import peapod.annotations.Out;
import peapod.annotations.Vertex;

@Vertex(Labels.TEXTNODE)
public abstract class TextNodeVF extends AlexandriaVF {
  private static final String NEXT_TEXTNODE = "next_textnode";

  public abstract void setText(String text);

  public abstract String getText();

  @Out
  @Edge(NEXT_TEXTNODE)
  public abstract TextNodeVF getNextTextNode();

  @In
  @Edge(NEXT_TEXTNODE)
  public abstract TextNodeVF setPrevTextNode(TextNodeVF previousTextNode);

  @In
  @Edge(NEXT_TEXTNODE)
  public abstract TextNodeVF getPrevTextNode();

}
