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

class AnnotationCreationCommand implements AnnotationCommand {
  private final AnnotationCreationRequest request;

  public AnnotationCreationCommand(AnnotationCreationRequest request) {
    this.request = request;
  }

  @Override
  public AlexandriaAnnotation execute(AnnotationService service) {
    final UUID uuid = providedUUID().orElse(UUID.randomUUID());
    final AlexandriaAnnotation annotation = service.createAnnotation(uuid, providedType(), optionalValue());

    annotation.setCreatedOn(providedCreatedOn().orElse(Instant.now()));

    streamAnnotations().map(service::readAnnotation).map(annotation::addAnnotation);

    return annotation;
  }

  private Optional<UUID> providedUUID() {
    return request.getId().map(UUIDParam::getValue);
  }

  private String providedType() {
    return request.getType().get();
  }

  private Optional<String> optionalValue() {
    return request.getValue();
  }

  private Stream<UUID> streamAnnotations() {
    return request.getAnnotations().map(Collection::stream).orElse(Stream.empty()) //
        .map(UUIDParam::getValue);
  }

  private Optional<Instant> providedCreatedOn() {
    return request.getCreatedOn().map(InstantParam::getValue);
  }
}
