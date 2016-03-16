package nl.knaw.huygens.alexandria.text;

/*
 * #%L
 * alexandria-main
 * =======
 * Copyright (C) 2015 - 2016 Huygens ING (KNAW)
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import nl.knaw.huygens.tei.XmlContext;

public class BLVContext extends XmlContext {
  private List<AnnotationData> annotationData = new ArrayList<>();
  private ElementTally elementTally = new ElementTally();
  private final List<String> validationErrors = new ArrayList<>();
  private Map<String, String> subresourceXPathMap = new HashMap<>();
  private Long textOffset = 1L;
  private Stack<Long> textOffsetStack = new Stack<>();

  public List<AnnotationData> getAnnotationData() {
    return annotationData;
  }

  public void setAnnotationData(List<AnnotationData> annotationData) {
    this.annotationData = annotationData;
  }

  public ElementTally getElementTally() {
    return elementTally;
  }

  public void setElementTally(ElementTally elementTally) {
    this.elementTally = elementTally;
  }

  public Map<String, String> getSubresourceXPathMap() {
    return subresourceXPathMap;
  }

  public void setSubresourceXPathMap(Map<String, String> subresourceXPathMap) {
    this.subresourceXPathMap = subresourceXPathMap;
  }

  public Long getTextOffset() {
    return textOffset;
  }

  public void setTextOffset(Long textOffset) {
    this.textOffset = textOffset;
  }

  public Stack<Long> getTextOffsetStack() {
    return textOffsetStack;
  }

  public void setTextOffsetStack(Stack<Long> textOffsetStack) {
    this.textOffsetStack = textOffsetStack;
  }

  public List<String> getValidationErrors() {
    return validationErrors;
  }

  public String substringOffsetXPath(Long start) {
    long length = getTextOffset() - start;
    return "substring(/," + start + "," + length + ")";
  }

  public void addToTextOffset(int length) {
    textOffset += length;
  }
}
