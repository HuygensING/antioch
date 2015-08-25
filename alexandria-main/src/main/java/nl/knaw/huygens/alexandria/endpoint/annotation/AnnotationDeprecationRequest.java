package nl.knaw.huygens.alexandria.endpoint.annotation;

import java.util.Optional;
import java.util.UUID;

import nl.knaw.huygens.alexandria.endpoint.CreationRequest;
import nl.knaw.huygens.alexandria.endpoint.ProvenancePrototype;
import nl.knaw.huygens.alexandria.model.AlexandriaAnnotation;
import nl.knaw.huygens.alexandria.model.AlexandriaAnnotationBody;
import nl.knaw.huygens.alexandria.model.TentativeAlexandriaProvenance;
import nl.knaw.huygens.alexandria.service.AlexandriaService;

public class AnnotationDeprecationRequest implements CreationRequest<AlexandriaAnnotation> {

  private final AnnotationUpdatePrototype prototype;

  private AlexandriaAnnotation originalAnnotation;

  public AnnotationDeprecationRequest(AlexandriaAnnotation originalAnnotation, AnnotationUpdatePrototype prototype) {
    this.prototype = prototype;
    this.originalAnnotation = originalAnnotation;
  }

  @Override
  public AlexandriaAnnotation execute(AlexandriaService service) {
    final TentativeAlexandriaProvenance provenance = providedProvenance().orElse(TentativeAlexandriaProvenance.createDefault());
    String type = originalAnnotation.getBody().getType();
    AlexandriaAnnotationBody body = service.findAnnotationBodyWithTypeAndValue(type, providedValue())//
        .orElseGet(() -> new AlexandriaAnnotationBody(UUID.randomUUID(), type, providedValue(), provenance));
    UUID annotationUuid = UUID.randomUUID();
    AlexandriaAnnotation newAnnotation = new AlexandriaAnnotation(annotationUuid, body, provenance);

    return service.deprecateAnnotation(originalAnnotation.getId(), newAnnotation);
  }

  private String providedValue() {
    return prototype.getValue();
  }

  private Optional<TentativeAlexandriaProvenance> providedProvenance() {
    return prototype.getProvenance().map(ProvenancePrototype::getValue);
  }

}
