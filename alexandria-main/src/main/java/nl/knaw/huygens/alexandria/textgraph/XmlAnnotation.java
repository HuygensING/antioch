package nl.knaw.huygens.alexandria.textgraph;

import java.util.Map;

import org.apache.commons.lang3.builder.CompareToBuilder;

public class XmlAnnotation implements Comparable<XmlAnnotation> {

  private String name;
  private Map<String, String> attributes;
  private boolean isMilestone;
  private Integer firstSegmentIndex;
  private Integer lastSegmentIndex;
  private int depth;

  public XmlAnnotation setName(String name) {
    this.name = name;
    return this;
  }

  public String getName() {
    return name;
  }

  public XmlAnnotation setAttributes(Map<String, String> attributes) {
    this.attributes = attributes;
    return this;
  }

  public Map<String, String> getAttributes() {
    return attributes;
  }

  public XmlAnnotation setMilestone(boolean bool) {
    this.isMilestone = bool;
    return this;
  }

  public boolean isMilestone() {
    return isMilestone;
  }

  public XmlAnnotation setDepth(int depth) {
    this.depth = depth;
    return this;
  }

  public int getDepth() {
    return depth;
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
        .append(this.depth, other.depth)//
        .build();
  }

  @Override
  public String toString() {
    return "XmlAnnotation[" + depth + ":" + firstSegmentIndex + "-" + lastSegmentIndex + "] <" + name + " " + attributes + (isMilestone ? "/>" : ">");
  }

}
