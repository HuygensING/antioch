package nl.knaw.huygens.alexandria.endpoint.resource;

import static java.util.Objects.requireNonNull;

import java.util.Optional;
import java.util.UUID;

import nl.knaw.huygens.Log;
import nl.knaw.huygens.alexandria.endpoint.CreationRequest;
import nl.knaw.huygens.alexandria.endpoint.ProvenancePrototype;
import nl.knaw.huygens.alexandria.endpoint.UUIDParam;
import nl.knaw.huygens.alexandria.model.AlexandriaResource;
import nl.knaw.huygens.alexandria.model.TentativeAlexandriaProvenance;
import nl.knaw.huygens.alexandria.service.AlexandriaService;

class SubResourceCreationRequest implements CreationRequest<AlexandriaResource> {

  private boolean subResource;
  private UUID parentId;
  private SubResourcePrototype prototype;
  private UUID uuid;

  public SubResourceCreationRequest(UUID parentId, SubResourcePrototype prototype) {
    this.parentId = parentId;
    this.prototype = prototype;
  }

  @Override
  public AlexandriaResource execute(AlexandriaService service) {
    Log.trace("executing, service=[{}]", service);

    uuid = providedUUID().orElse(UUID.randomUUID());

    String sub = providedSub();
    TentativeAlexandriaProvenance provenance = providedProvenance().orElse(TentativeAlexandriaProvenance.createDefault());
    service.createSubResource(uuid, parentId, sub, provenance);

    return service.readSubResource(uuid).get();
  }

  public boolean wasExecutedAsIs() {
    final boolean wasExecutedAsIs = providedUUID().isPresent();// && providedProvenance().isPresent();
    Log.trace("wasExecutedAsIs: {}", wasExecutedAsIs);
    return wasExecutedAsIs;
  }

  public boolean newResourceWasCreated() {
    Log.trace("newResourceWasCreated: {}", subResource);
    return subResource;
  }

  public UUID getUUID() {
    return uuid;
  }

  private String providedSub() {
    return requireNonNull(prototype.getSub(), "Required 'sub' field was not validated for being non-null");
  }

  private Optional<UUID> providedUUID() {
    return Optional.ofNullable(prototype.getId()).map(UUIDParam::getValue);
  }

  private Optional<TentativeAlexandriaProvenance> providedProvenance() {
    return prototype.getProvenance().map(ProvenancePrototype::getValue);
  }
}
