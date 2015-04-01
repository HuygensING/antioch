package nl.knaw.huygens.alexandria.endpoint.resource;

import static java.time.Instant.now;

import java.time.Instant;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import nl.knaw.huygens.alexandria.endpoint.InstantParam;
import nl.knaw.huygens.alexandria.endpoint.UUIDParam;
import nl.knaw.huygens.alexandria.model.AlexandriaResource;
import nl.knaw.huygens.alexandria.service.ResourceService;

class ResourceCreationCommand {
  private final ResourcePrototype prototype;

  ResourceCreationCommand(ResourcePrototype prototype) {
    this.prototype = prototype;
  }

  public AlexandriaResource execute(ResourceService service) {
    final AlexandriaResource resource = service.createResource(providedUUID().orElse(UUID.randomUUID()));

    // TODO: figure out how to get a hold of the annotation, or how to use an annotation's UUID to add it
    // streamAnnotations().map(annotationService::readAnnotation).map(resource::addAnnotation);

    resource.setCreatedOn(providedCreatedOn().orElse(now()));

    return resource;
  }

  public boolean requiredIntervention() {
    final boolean protoTypeProvidedId = prototype.getId().isPresent();
    final boolean protoTypeProvidedCreatedOn = prototype.getCreatedOn().isPresent();
    return !protoTypeProvidedId || !protoTypeProvidedCreatedOn;
  }

  private Optional<UUID> providedUUID() {
    return prototype.getId().map(UUIDParam::getValue);
  }

  private Stream<UUID> streamAnnotations() {
    return prototype.getAnnotations().map(Collection::stream).orElse(Stream.empty()) //
            .map(UUIDParam::getValue);
  }

  private Optional<Instant> providedCreatedOn() {
    return prototype.getCreatedOn().map(InstantParam::getValue);
  }
}
