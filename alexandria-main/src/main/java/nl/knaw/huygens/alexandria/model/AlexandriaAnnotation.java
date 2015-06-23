package nl.knaw.huygens.alexandria.model;

import java.net.URI;
import java.util.UUID;

public class AlexandriaAnnotation extends AbstractAnnotatable {
  private final UUID id;

  private final AlexandriaAnnotationBody body;

  private final AlexandriaProvenance provenance;

  private URI annotatableURI;

  private AccountablePointer annotatedPointer;

  public AlexandriaAnnotation(UUID id, AlexandriaAnnotationBody body, TentativeAlexandriaProvenance provenance) {
    this.id = id;
    this.body = body;
    this.provenance = provenance.bind(this);
  }

  @Override
  public UUID getId() {
    return id;
  }

  public AlexandriaAnnotationBody getBody() {
    return body;
  }

  @Override
  public AlexandriaProvenance getProvenance() {
    return provenance;
  }

  public URI getAnnotatableURI() {
    return annotatableURI;
  }

  public void setAnnotatablePointer(AccountablePointer pointer) {
    this.annotatedPointer = pointer;
  }

  public AccountablePointer getAnnotatablePointer() {
    return annotatedPointer;
  }

}
