package nl.knaw.huygens.alexandria.api.model.text;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

import nl.knaw.huygens.alexandria.api.JsonTypeNames;
import nl.knaw.huygens.alexandria.api.model.JsonWrapperObject;

@JsonTypeName(JsonTypeNames.TEXTANNOTATION)
@JsonInclude(Include.NON_NULL)
public class TextRangeAnnotation extends JsonWrapperObject {
  public static final String RESPONSIBILITY_ATTRIBUTE = "resp";

  public static class Position {
    @JsonProperty("xml:id")
    String xmlId;

    Integer offset;
    Integer length;

    public String getXmlId() {
      return xmlId;
    }

    public Position setXmlId(String xmlId) {
      this.xmlId = xmlId;
      return this;
    }

    public Optional<Integer> getOffset() {
      return Optional.ofNullable(offset);
    }

    public Position setOffset(Integer offset) {
      this.offset = offset;
      return this;
    }

    public Optional<Integer> getLength() {
      return Optional.ofNullable(length);
    }

    public Position setLength(Integer length) {
      this.length = length;
      return this;
    }

    @Override
    public String toString() {
      return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }

  }

  @JsonProperty("id")
  private UUID uuid;

  private String name;
  private String annotator;
  private Position position;
  private Map<String, String> attributes = new HashMap<>();

  public TextRangeAnnotation setId(UUID uuid) {
    this.uuid = uuid;
    return this;
  }

  public UUID getId() {
    return uuid;
  }

  public TextRangeAnnotation setName(String name) {
    this.name = name;
    return this;
  }

  public String getName() {
    return name;
  }

  public TextRangeAnnotation setAnnotator(String annotator) {
    this.annotator = annotator;
    return this;
  }

  public String getAnnotator() {
    return annotator;
  }

  public TextRangeAnnotation setPosition(Position position) {
    this.position = position;
    return this;
  }

  public Position getPosition() {
    return position;
  }

  public TextRangeAnnotation setAttributes(Map<String, String> attributes) {
    this.attributes = attributes;
    return this;
  }

  public Map<String, String> getAttributes() {
    return attributes;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
  }

}
