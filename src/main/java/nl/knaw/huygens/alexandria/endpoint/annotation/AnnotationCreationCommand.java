package nl.knaw.huygens.alexandria.endpoint.annotation;

import static java.time.Instant.now;
import static java.util.UUID.randomUUID;

import java.time.Instant;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import nl.knaw.huygens.alexandria.endpoint.InstantParam;
import nl.knaw.huygens.alexandria.endpoint.UUIDParam;
import nl.knaw.huygens.alexandria.model.AlexandriaAnnotation;
import nl.knaw.huygens.alexandria.service.AnnotationService;

class AnnotationCreationCommand {
  private final AnnotationPrototype prototype;

  public AnnotationCreationCommand(AnnotationPrototype prototype) {
    this.prototype = prototype;
  }

  public AlexandriaAnnotation execute(AnnotationService service) {
    final UUID uuid = providedUUID().orElse(randomUUID());
    final AlexandriaAnnotation annotation = service.createAnnotation(uuid, providedType(), optionalValue());

    annotation.setCreatedOn(providedCreatedOn().orElse(now()));

    streamAnnotations().map(service::readAnnotation).forEach(annotation::addAnnotation);

    return annotation;
  }

  public boolean requiredIntervention() {
    boolean generateUUID = !prototype.getId().isPresent();
    boolean generateCreatedOn = !prototype.getCreatedOn().isPresent();
    return generateUUID || generateCreatedOn;
  }

  private Optional<UUID> providedUUID() {
    return prototype.getId().map(UUIDParam::getValue);
  }

  private String providedType() {
    return prototype.getType().get();
  }

  private Optional<String> optionalValue() {
    return prototype.getValue();
  }

  private Stream<UUID> streamAnnotations() {
    return prototype.getAnnotations().map(Collection::stream).orElse(Stream.empty()) //
        .map(UUIDParam::getValue);
  }

  private Optional<Instant> providedCreatedOn() {
    return prototype.getCreatedOn().map(InstantParam::getValue);
  }
}
