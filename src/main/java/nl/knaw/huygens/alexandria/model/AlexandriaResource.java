package nl.knaw.huygens.alexandria.model;

import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

public class AlexandriaResource {
  private final UUID id;

  private final Set<AlexandriaAnnotation> annotations = new HashSet<>();

  private Instant createdOn;

  private String ref;

  public AlexandriaResource() {
    this(UUID.randomUUID());
  }

  public AlexandriaResource(UUID id) {
    this(id, Instant.now());
  }

  public AlexandriaResource(UUID id, Instant createdOn) {
    this.id = id;
    this.createdOn = createdOn;
  }

  public UUID getId() {
    return id;
  }

  public Instant getCreatedOn() {
    return createdOn;
  }

  public void setCreatedOn(Instant createdOn) {
    this.createdOn = createdOn;
  }

  public Set<AlexandriaAnnotation> getAnnotations() {
    return Collections.unmodifiableSet(annotations);
  }

  public Stream<AlexandriaAnnotation> streamAnnotations() {
    return annotations.stream();
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
