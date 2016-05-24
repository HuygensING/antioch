package nl.knaw.huygens.alexandria.api.model.text;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

import nl.knaw.huygens.alexandria.api.JsonTypeNames;
import nl.knaw.huygens.alexandria.api.model.JsonWrapperObject;

@JsonTypeName(JsonTypeNames.TEXTANNOTATION)
@JsonInclude(Include.NON_NULL)
public class ResourceTextAnnotation extends JsonWrapperObject {

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

    public Integer getOffset() {
      return offset;
    }

    public Position setOffset(Integer offset) {
      this.offset = offset;
      return this;
    }

    public Integer getLength() {
      return length;
    }

    public Position setLength(Integer length) {
      this.length = length;
      return this;
    }
  }

  private UUID uuid;
  private String name;
  private String annotator;
  private Position position;

  public ResourceTextAnnotation setId(UUID uuid) {
    this.uuid = uuid;
    return this;
  }

  public UUID getId() {
    return uuid;
  }

  public ResourceTextAnnotation setName(String name) {
    this.name = name;
    return this;
  }

  public String getName() {
    return name;
  }

  public ResourceTextAnnotation setAnnotator(String annotator) {
    this.annotator = annotator;
    return this;
  }

  public String getAnnotator() {
    return annotator;
  }

  public ResourceTextAnnotation setPosition(Position position) {
    this.position = position;
    return this;
  }

  public Position getPosition() {
    return position;
  }

}
