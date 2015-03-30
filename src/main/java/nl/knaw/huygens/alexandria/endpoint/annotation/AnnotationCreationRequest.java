package nl.knaw.huygens.alexandria.endpoint.annotation;

import java.time.Instant;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import nl.knaw.huygens.alexandria.endpoint.InstantParam;
import nl.knaw.huygens.alexandria.endpoint.UUIDParam;
import nl.knaw.huygens.alexandria.model.AlexandriaAnnotation;
import nl.knaw.huygens.alexandria.service.AnnotationService;

class AnnotationCreationRequest implements AnnotationRequest {
  private final AnnotationCreationParameters parameters;

  public AnnotationCreationRequest(AnnotationCreationParameters parameters) {
    this.parameters = parameters;
  }

  @Override
  public AlexandriaAnnotation execute(AnnotationService service) {
    final UUID uuid = providedUUID().orElse(UUID.randomUUID());
    final String type = providedType();
    final Optional<String> value = optionalValue();

    final AlexandriaAnnotation annotation = service.createAnnotation(uuid, type, value);

    annotation.setCreatedOn(providedCreatedOn().orElse(Instant.now()));

    streamAnnotations().map(service::readAnnotation).map(annotation::addAnnotation);

    return annotation;
  }

  private Optional<UUID> providedUUID() {
    return parameters.getId().map(UUIDParam::getValue);
  }

  private String providedType() {
    return parameters.getType().get();
  }

  private Optional<String> optionalValue() {
    return parameters.getValue();
  }

  private Stream<UUID> streamAnnotations() {
    return parameters.getAnnotations().map(Collection::stream).orElse(Stream.empty()) //
        .map(UUIDParam::getValue);
  }

  private Optional<Instant> providedCreatedOn() {
    return parameters.getCreatedOn().map(InstantParam::getValue);
  }
}
