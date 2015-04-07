package nl.knaw.huygens.alexandria.endpoint.resource;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;

import nl.knaw.huygens.alexandria.endpoint.UUIDParam;
import nl.knaw.huygens.alexandria.exception.BadRequestException;
import nl.knaw.huygens.alexandria.exception.IdMismatchException;
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

  public ResourceCreationCommand withoutId(ResourcePrototype protoType) {
    requireProtoType(protoType);

    // TODO: validate protoType values
    validateRef(protoType);

    return new ResourceCreationCommand(protoType);
  }

  public ResourceCreationCommand ofExistingId(ResourcePrototype protoType, UUID uuid) {
    requireProtoType(protoType);
    validateParamIdAgainstProtoTypeId(protoType, uuid);
    validateRef(protoType);

    return new ResourceCreationCommand(protoType);
  }

  private void requireProtoType(ResourcePrototype protoType) {
    Optional.ofNullable(protoType).orElseThrow(missingBodyException());
  }

  private void validateParamIdAgainstProtoTypeId(ResourcePrototype protoType, UUID uuid) {
    LOG.debug("createResourceAtSpecificID: paramId=[{}] vs protoType.id=[{}]", uuid, protoType.getId());
    protoType.getId().map(UUIDParam::getValue).ifPresent(mustEqual(uuid));
  }

  private void validateRef(ResourcePrototype protoType) {
    LOG.trace("validating ref");
    protoType.getRef().orElseThrow(missingRefException());
  }

  private Consumer<? super UUID> mustEqual(final UUID someId) {
    return suspectId -> {
      if (!suspectId.equals(someId)) {
        throw new IdMismatchException(someId, suspectId);
      }
    };
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
