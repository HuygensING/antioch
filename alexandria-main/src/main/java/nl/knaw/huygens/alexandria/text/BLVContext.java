package nl.knaw.huygens.alexandria.text;

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
