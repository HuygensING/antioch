package nl.knaw.huygens.alexandria.model;

import static java.time.Instant.now;
import static nl.knaw.huygens.alexandria.util.OfNullableRenaming.ifPresent;

import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeInfo(use = Id.NAME, include = As.WRAPPER_OBJECT)
@JsonTypeName("resource")
public class AlexandriaResource {
  private final UUID id;

  private final Set<AlexandriaAnnotation> annotations = new HashSet<>();

  private final Instant createdOn;

  private String ref;

  public AlexandriaResource() {
    this(UUID.randomUUID());
  }

  public AlexandriaResource(UUID id) {
    this(id, now());
  }

  public AlexandriaResource(UUID id, Instant createdOn) {
    this.id = id;
    this.createdOn = createdOn;
  }

  public AlexandriaResource(AlexandriaResource protoType) {
    id = ifPresent(protoType.id).orElse(UUID.randomUUID());
    ref = protoType.ref;
    createdOn = ifPresent(protoType.createdOn).orElse(now());
    annotations.addAll(protoType.annotations);
  }

  public UUID getId() {
    return id;
  }

  public String getCreatedOn() {
    return createdOn.toString();
  }

  public Set<AlexandriaAnnotation> getAnnotations() {
    return Collections.unmodifiableSet(annotations);
  }

  public void addAnnotation(AlexandriaAnnotation annotation) {
    annotations.add(annotation);
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "[id=" + getId() + ",createdOn=" + createdOn + "]";
  }

  public String getRef() {
    return ref;
  }

  public void setRef(String ref) {
    this.ref = ref;
  }
}
