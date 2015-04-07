package nl.knaw.huygens.alexandria.endpoint.resource;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

import nl.knaw.huygens.alexandria.exception.BadRequestException;
import nl.knaw.huygens.alexandria.service.ResourceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResourceCreationCommandBuilder {
  private static final String MISSING_RESOURCE_BODY_MESSAGE = "Missing or empty resource request body";
  private static final String MISSING_REF_MESSAGE = "Missing required 'ref' field in resource body";

  private static final Logger LOG = LoggerFactory.getLogger(ResourceCreationCommandBuilder.class);

  public static ResourceCreationCommandBuilder servedBy(ResourceService service) {
    return new ResourceCreationCommandBuilder(service);
  }

  private final ResourceService service;

  protected ResourceCreationCommandBuilder(ResourceService service) {
    this.service = Objects.requireNonNull(service, "ResourceService MUST not be null");
  }

  public ResourceCreationCommand build(ResourcePrototype prototype) {
    Optional.ofNullable(prototype).orElseThrow(missingBodyException());

    // TODO: validate prototype values
    validateRef(prototype);

    return new ResourceCreationCommand(prototype);
  }

  private void validateRef(ResourcePrototype prototype) {
    LOG.trace("validating ref");
    prototype.getRef().orElseThrow(missingRefException());
  }

  private Supplier<? extends BadRequestException> missingRefException() {
    return () -> badRequestException(MISSING_REF_MESSAGE);
  }

  protected Supplier<BadRequestException> missingBodyException() {
    return () -> badRequestException(MISSING_RESOURCE_BODY_MESSAGE);
  }

  protected BadRequestException badRequestException(String message) {
    LOG.trace(message);
    return new BadRequestException(message);
  }

}
