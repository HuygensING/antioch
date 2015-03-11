package nl.knaw.huygens.alexandria.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeInfo(use = Id.NAME, include = As.WRAPPER_OBJECT)
@JsonTypeName("annotation")
public class AlexandriaAnnotation {
  private final UUID id;

  private final Set<AlexandriaAnnotation> annotations = new HashSet<>();

  public AlexandriaAnnotation() {
    this(UUID.randomUUID());
  }

  public AlexandriaAnnotation(UUID id) {
    this.id = id;
  }

  public UUID getId() {
    return id;
  }

  public Set<AlexandriaAnnotation> getAnnotations() {
    return Collections.unmodifiableSet(annotations);
  }

  public boolean addAnnotation(AlexandriaAnnotation annotation) {
    return annotations.add(annotation);
  }

  public boolean removeAnnotation(AlexandriaAnnotation annotation) {
    return annotations.remove(annotation);
  }
}
