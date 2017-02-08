package nl.knaw.huygens.alexandria.textgraph;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TextGraphSegment {
  List<TextAnnotation> textAnnotationsToOpen = new ArrayList<>();
  TextAnnotation milestoneTextAnnotation = null;
  String textSegment = "";
  List<TextAnnotation> textAnnotationsToClose = new ArrayList<>();

  public List<TextAnnotation> getTextAnnotationsToOpen() {
    return textAnnotationsToOpen;
  }

  public void setAnnotationsToOpen(List<TextAnnotation> textAnnotationsToOpen) {
    this.textAnnotationsToOpen = textAnnotationsToOpen;
  }

  public Optional<TextAnnotation> getMilestoneTextAnnotation() {
    return Optional.ofNullable(milestoneTextAnnotation);
  }

  public void setMilestoneAnnotation(TextAnnotation milestoneTextAnnotation) {
    this.milestoneTextAnnotation = milestoneTextAnnotation;
  }

  public String getTextSegment() {
    return textSegment;
  }

  public void setTextSegment(String textSegment) {
    this.textSegment = textSegment;
  }

  public List<TextAnnotation> getTextAnnotationsToClose() {
    return textAnnotationsToClose;
  }

  public void setAnnotationsToClose(List<TextAnnotation> textAnnotationsToClose) {
    this.textAnnotationsToClose = textAnnotationsToClose;
  }

  // public boolean isMilestone() {
  // return textSegment.isEmpty() //
  // && textAnnotationsToClose.size() == 1 //
  // && textAnnotationsToOpen.size() == 1 //
  // && textAnnotationsToClose.get(0).equals(textAnnotationsToOpen.get(0));
  // }
  //
  // public TextAnnotation getMilestone() {
  // return isMilestone() ? textAnnotationsToOpen.get(0) : null;
  // }
}
