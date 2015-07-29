package nl.knaw.huygens.alexandria.endpoint.annotation;

import java.util.Optional;
import java.util.UUID;

import nl.knaw.huygens.alexandria.endpoint.CreationRequest;
import nl.knaw.huygens.alexandria.endpoint.ProvenancePrototype;
import nl.knaw.huygens.alexandria.model.AlexandriaAnnotation;
import nl.knaw.huygens.alexandria.model.AlexandriaAnnotationBody;
import nl.knaw.huygens.alexandria.model.AlexandriaState;
import nl.knaw.huygens.alexandria.model.TentativeAlexandriaProvenance;
import nl.knaw.huygens.alexandria.service.AlexandriaService;

public class AnnotationDeprecationRequest implements CreationRequest<AlexandriaAnnotation> {

  private final AnnotationPrototype prototype;

  private UUID oldAnnotationId;

  public AnnotationDeprecationRequest(UUID annotationToDeprecateId, AnnotationPrototype prototype) {
    this.prototype = prototype;
    this.oldAnnotationId = annotationToDeprecateId;
  }

  @Override
  public AlexandriaAnnotation execute(AlexandriaService service) {
    final TentativeAlexandriaProvenance provenance = providedProvenance().orElse(TentativeAlexandriaProvenance.createDefault());
    String type = providedType().orElse("");
    AlexandriaAnnotationBody body = service.findAnnotationBodyWithTypeAndValue(type, providedValue())//
        .orElseGet(() -> new AlexandriaAnnotationBody(UUID.randomUUID(), type, providedValue(), provenance));
    UUID annotationUuid = UUID.randomUUID();
    AlexandriaAnnotation newAnnotation = new AlexandriaAnnotation(annotationUuid, body, provenance);

    return service.deprecateAnnotation(oldAnnotationId, newAnnotation);
    // return service.annotate(oldAnnotation.getAnnotatablePointer(), body, provenance);
  }

  private Optional<String> providedType() {
    return prototype.getType();
  }

  private String providedValue() {
    return prototype.getValue();
  }

  private Optional<TentativeAlexandriaProvenance> providedProvenance() {
    return prototype.getProvenance().map(ProvenancePrototype::getValue);
  }

}
