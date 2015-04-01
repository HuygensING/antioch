package nl.knaw.huygens.alexandria.endpoint.resource;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

import nl.knaw.huygens.alexandria.exception.BadRequestException;
import nl.knaw.huygens.alexandria.service.ResourceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResourceCreationCommandBuilder {
  public static final String MISSING_RESOURCE_BODY_MESSAGE = "Missing or empty resource request body";

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

    return new ResourceCreationCommand(prototype);
  }

  protected Supplier<BadRequestException> missingBodyException() {
    return () -> badRequestException(MISSING_RESOURCE_BODY_MESSAGE);
  }

  protected BadRequestException badRequestException(String message) {
    LOG.trace(message);
    return new BadRequestException(message);
  }

}
