package nl.knaw.huygens.alexandria.textgraph;

import java.util.Map;

import org.apache.commons.lang3.builder.CompareToBuilder;

public class XmlAnnotation extends TextAnnotation implements Comparable<XmlAnnotation> {
  private boolean isMilestone;
  private Integer firstSegmentIndex;
  private Integer lastSegmentIndex;

  public XmlAnnotation(String name, Map<String, String> attributes, int depth) {
    super(name, attributes, depth);
  }

  public XmlAnnotation setMilestone(boolean bool) {
    this.isMilestone = bool;
    return this;
  }

  public boolean isMilestone() {
    return isMilestone;
  }

  public XmlAnnotation setFirstSegmentIndex(Integer firstSegmentIndex) {
    this.firstSegmentIndex = firstSegmentIndex;
    return this;
  }

  public Integer getFirstSegmentIndex() {
    return firstSegmentIndex;
  }

  public XmlAnnotation setLastSegmentIndex(Integer lastSegmentIndex) {
    this.lastSegmentIndex = lastSegmentIndex;
    return this;
  }

  public Integer getLastSegmentIndex() {
    return lastSegmentIndex;
  }

  @Override
  public int compareTo(XmlAnnotation other) {
    return new CompareToBuilder()//
        .append(this.firstSegmentIndex, other.firstSegmentIndex)//
        .append(other.lastSegmentIndex, this.lastSegmentIndex)//
        .append(getDepth(), other.getDepth())//
        .build();
  }

  @Override
  public String toString() {
    return "XmlAnnotation[" + getDepth() + ":" + firstSegmentIndex + "-" + lastSegmentIndex + "] <" + getName() + " " + getAttributes() + (isMilestone ? "/>" : ">");
  }

}