package nl.knaw.huygens.alexandria.model;

import java.util.UUID;

public class AlexandriaAnnotation extends AbstractAnnotatable {

  private final AlexandriaAnnotationBody body;
  private AccountablePointer<?> annotatedPointer;

  public AlexandriaAnnotation(UUID id, AlexandriaAnnotationBody body, TentativeAlexandriaProvenance provenance) {
    super(id, provenance);
    this.body = body;
  }

  public AlexandriaAnnotationBody getBody() {
    return body;
  }

  public void setAnnotatablePointer(AccountablePointer<?> pointer) {
    this.annotatedPointer = pointer;
  }

  public AccountablePointer<?> getAnnotatablePointer() {
    return annotatedPointer;
  }

}
