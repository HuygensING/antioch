package nl.knaw.huygens.alexandria.api.model.text;

/*
 * #%L
 * alexandria-api
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

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeName;

import nl.knaw.huygens.alexandria.api.JsonTypeNames;
import nl.knaw.huygens.alexandria.api.model.JsonWrapperObject;

@JsonTypeName(JsonTypeNames.TEXTANNOTATION)
@JsonInclude(Include.NON_EMPTY)
@JsonPropertyOrder({ "id", "name" })
public class TextRangeAnnotation extends JsonWrapperObject {
  public static final String RESPONSIBILITY_ATTRIBUTE = "resp";

  @JsonInclude(Include.NON_EMPTY)
  public static class Position {
    @JsonProperty("xml:id")
    private String xmlId;

    private Integer offset;
    private Integer length;

    private UUID targetAnnotationId;

    public Position setXmlId(String xmlId) {
      this.xmlId = xmlId;
      return this;
    }

    public Optional<String> getXmlId() {
      return Optional.ofNullable(xmlId);
    }

    public Position setTargetAnnotationId(UUID annotationId) {
      this.targetAnnotationId = annotationId;
      return this;
    }

    public Optional<UUID> getTargetAnnotationId() {
      return Optional.ofNullable(targetAnnotationId);
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

  public static class AbsolutePosition {
    private String xmlId;

    private Integer offset;
    private Integer length;

    public AbsolutePosition setXmlId(String xmlId) {
      this.xmlId = xmlId;
      return this;
    }

    public String getXmlId() {
      return xmlId;
    }

    public Integer getOffset() {
      return offset;
    }

    public AbsolutePosition setOffset(Integer offset) {
      this.offset = offset;
      return this;
    }

    public Integer getLength() {
      return length;
    }

    public AbsolutePosition setLength(Integer length) {
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

  private Integer revision;
  private String name;
  private String annotator;
  private Position position; // may contain targetAnnotatioId, or doesn't contain offset/length

  @JsonIgnore
  private AbsolutePosition absolutePosition; // always has xmlId + (calculated) offset/length

  private Map<String, String> attributes = new HashMap<>();
  @JsonIgnore
  private Boolean useOffset;

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

  public TextRangeAnnotation setAbsolutePosition(AbsolutePosition position) {
    this.absolutePosition = position;
    return this;
  }

  public AbsolutePosition getAbsolutePosition() {
    return absolutePosition;
  }

  public TextRangeAnnotation setAttributes(Map<String, String> attributes) {
    this.attributes = attributes;
    return this;
  }

  public Map<String, String> getAttributes() {
    return attributes;
  }

  public TextRangeAnnotation setUseOffset(boolean useOffset) {
    this.useOffset = useOffset;
    return this;
  }

  public boolean getUseOffset() {
    if (useOffset == null) {
      useOffset = position.getOffset().isPresent();
    }
    return useOffset;
  }

  public TextRangeAnnotation setRevision(Integer revision) {
    this.revision = revision;
    return this;
  }

  public Integer getRevision() {
    if (revision == null) {
      return 0;
    }
    return revision;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
  }

}
