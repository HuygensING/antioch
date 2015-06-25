package nl.knaw.huygens.alexandria.endpoint.annotationbody;

import static java.util.Objects.requireNonNull;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import nl.knaw.huygens.alexandria.endpoint.CreationRequest;
import nl.knaw.huygens.alexandria.model.AlexandriaAnnotationBody;
import nl.knaw.huygens.alexandria.model.AlexandriaProvenance;
import nl.knaw.huygens.alexandria.model.TentativeAlexandriaProvenance;
import nl.knaw.huygens.alexandria.service.AlexandriaService;

public class AnnotationBodyCreationRequest implements CreationRequest<AlexandriaAnnotationBody> {
  private final AnnotationBodyPrototype prototype;

  private boolean wasCreated;

  public AnnotationBodyCreationRequest(AnnotationBodyPrototype prototype) {
    this.prototype = prototype;
  }

  @Override
  public AlexandriaAnnotationBody execute(AlexandriaService service) {
    TentativeAlexandriaProvenance provenance = new TentativeAlexandriaProvenance(AlexandriaProvenance.DEFAULT_WHO, Instant.now(), AlexandriaProvenance.DEFAULT_WHY);
    return service.createAnnotationBody(providedUUID(), providedType(), providedValue(), provenance);
  }

  private String providedValue() {
    return requireNonNull(prototype.getValue(), "Required 'value' field was not validated for being non-null");
  }

  private Optional<String> providedType() {
    return prototype.getType();
  }

  public boolean wasCreated() {
    return wasCreated;
  }

  private UUID providedUUID() {
    return prototype.getId().getValue();
  }

}
