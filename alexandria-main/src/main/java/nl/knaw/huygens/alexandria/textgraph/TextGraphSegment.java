package nl.knaw.huygens.alexandria.textgraph;

import java.util.List;

import org.assertj.core.util.Lists;

public class TextGraphSegment {
  List<TextAnnotation> textAnnotationsToOpen = Lists.newArrayList();
  String textSegment = "";
  List<TextAnnotation> textAnnotationsToClose = Lists.newArrayList();

  public List<TextAnnotation> gettextAnnotationsToOpen() {
    return textAnnotationsToOpen;
  }

  public void setAnnotationsToOpen(List<TextAnnotation> textAnnotationsToOpen) {
    this.textAnnotationsToOpen = textAnnotationsToOpen;
  }

  public String getTextSegment() {
    return textSegment;
  }

  public void setTextSegment(String textSegment) {
    this.textSegment = textSegment;
  }

  public List<TextAnnotation> gettextAnnotationsToClose() {
    return textAnnotationsToClose;
  }

  public void setAnnotationsToClose(List<TextAnnotation> textAnnotationsToClose) {
    this.textAnnotationsToClose = textAnnotationsToClose;
  }
}
