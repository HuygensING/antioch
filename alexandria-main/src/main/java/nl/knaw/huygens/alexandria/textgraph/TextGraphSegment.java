package nl.knaw.huygens.alexandria.textgraph;

/*
 * #%L
 * alexandria-main
 * =======
 * Copyright (C) 2015 - 2017 Huygens ING (KNAW)
 * =======
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

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
