package nl.knaw.huygens.alexandria.model;

import java.util.UUID;

public class AlexandriaAnnotation extends AbstractAnnotatable {
  private final UUID id;

  private final AlexandriaAnnotationBody body;

  private final AlexandriaProvenance provenance;

  private AbstractAnnotatable annotatable;

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

  public void setAnnotatable(AbstractAnnotatable annotatable) {
    this.annotatable = annotatable;
  }

  public AbstractAnnotatable getAnnotatable() {
    return annotatable;
  }

}
