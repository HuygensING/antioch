package nl.knaw.huygens.alexandria.model;

import java.time.Instant;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.google.common.collect.Sets;

@JsonTypeInfo(use = Id.NAME, include = As.WRAPPER_OBJECT)
@JsonTypeName("annotation")
public class AlexandriaAnnotation {
  private final UUID id;

  private final String type;

  private final String value;

  private final Set<AlexandriaAnnotation> annotations;

  private Instant createdOn;

  public AlexandriaAnnotation(UUID id, String type, String value) {
    this.id = id;
    this.type = type;
    this.value = value;
    this.annotations = Sets.newHashSet();
  }

  public UUID getId() {
    return id;
  }

  public String getType() {
    return type;
  }

  public String getValue() {
    return value;
  }

  public Set<AlexandriaAnnotation> getAnnotations() {
    return Collections.unmodifiableSet(annotations);
  }

  public Stream<AlexandriaAnnotation> streamAnnotations() {
    return annotations.stream();
  }

  public boolean addAnnotation(AlexandriaAnnotation annotation) {
    return annotations.add(annotation);
  }

  public boolean removeAnnotation(AlexandriaAnnotation annotation) {
    return annotations.remove(annotation);
  }

  public Instant getCreatedOn() {
    return createdOn;
  }

  public void setCreatedOn(Instant createdOn) {
    this.createdOn = createdOn;
  }
}
