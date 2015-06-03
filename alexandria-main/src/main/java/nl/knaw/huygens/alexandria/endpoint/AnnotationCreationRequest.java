package nl.knaw.huygens.alexandria.endpoint;

import static java.util.Objects.requireNonNull;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import nl.knaw.huygens.alexandria.endpoint.annotation.AnnotationPrototype;
import nl.knaw.huygens.alexandria.model.AlexandriaAnnotation;
import nl.knaw.huygens.alexandria.model.AlexandriaAnnotationBody;
import nl.knaw.huygens.alexandria.model.AlexandriaProvenance;
import nl.knaw.huygens.alexandria.model.AlexandriaResource;
import nl.knaw.huygens.alexandria.model.TentativeAlexandriaProvenance;
import nl.knaw.huygens.alexandria.service.AlexandriaService;

public class AnnotationCreationRequest {

  private final AnnotationPrototype prototype;

  private Optional<AlexandriaResource> resource;
  private Optional<AlexandriaAnnotation> annotation;

  public AnnotationCreationRequest(AlexandriaResource resource, AnnotationPrototype prototype) {
    this.prototype = prototype;
    this.resource = Optional.of(resource);
  }

  public AnnotationCreationRequest(AlexandriaAnnotation annotation, AnnotationPrototype prototype) {
    this.prototype = prototype;
    this.annotation = Optional.of(annotation);
  }

  public AlexandriaAnnotation execute(AlexandriaService service) {
    final TentativeAlexandriaProvenance provenance = new TentativeAlexandriaProvenance(AlexandriaProvenance.DEFAULT_WHO, providedCreatedOn().orElse(Instant.now()), AlexandriaProvenance.DEFAULT_WHY);

    AlexandriaAnnotationBody body = null;
    UUID bodyId = providedId().getValue();
    body = service.readAnnotationBody(bodyId); // must exist, checked by @ExistingAnnotationBody. TODO: transactionize?

    if (resource.isPresent()) {
      return service.annotate(resource.get(), body, provenance);
    }

    return service.annotate(annotation.get(), body, provenance);
  }

  private UUIDParam providedId() {
    return requireNonNull(prototype.getAnnotationBodyId(), "Required 'value' field was not validated for being non-null");
  }

  private Optional<Instant> providedCreatedOn() {
    return prototype.getCreatedOn().map(InstantParam::getValue);
  }
}
