package nl.knaw.huygens.alexandria.textgraph;

import java.util.Map;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class TextAnnotation {
  public static class Properties {
    public static final String name = "name";
    public static final String attribute_keys = "attribute_keys";
    public static final String attribute_values = "attribute_values";
    public static final String depth = "depth";
    public static final String xmlid = "xml_id";
  }

  private String name;
  private Map<String, String> attributes;
  private int depth;
  private Object id;
  private String xmlId;

  public TextAnnotation(String name, Map<String, String> attributes, int depth) {
    this.name = name;
    this.attributes = attributes;
    xmlId = this.attributes.get("xml:id");
    this.depth = depth;
  }

  public void setId(Object object) {
    this.id = object;
  }

  public Object getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getXmlId() {
    return xmlId;
  }

  public Map<String, String> getAttributes() {
    return attributes;
  }

  // TODO:: does this need to be stored?
  public void setDepth(int newDepth) {
    this.depth = newDepth;
  }

  public Integer getDepth() {
    return depth;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
  }

  @Override
  public int hashCode() {
    return HashCodeBuilder.reflectionHashCode(this, false);
  }

  @Override
  public boolean equals(Object other) {
    return EqualsBuilder.reflectionEquals(this, other, false);
  }

}
