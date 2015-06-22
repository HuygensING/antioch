package nl.knaw.huygens.alexandria.endpoint.resource;

import static java.util.Objects.requireNonNull;

import java.util.Optional;
import java.util.UUID;

import nl.knaw.huygens.Log;
import nl.knaw.huygens.alexandria.endpoint.ProvenancePrototype;
import nl.knaw.huygens.alexandria.endpoint.UUIDParam;
import nl.knaw.huygens.alexandria.model.AlexandriaResource;
import nl.knaw.huygens.alexandria.model.TentativeAlexandriaProvenance;
import nl.knaw.huygens.alexandria.service.AlexandriaService;

class ResourceCreationRequest {

  private final ResourcePrototype prototype;
  private boolean resourceCreated;

  private UUID uuid;

  ResourceCreationRequest(ResourcePrototype prototype) {
    this.prototype = prototype;
  }

  /*
   * Yuck!
   * TODO: introduce CreationRequest interface with execute() (and maybe later wasExecutedAsIs et al.)
   * two classes: ResourceCreationRequest implements CreationRequest and SubResourceCreationRequest implements etc.
   * ResourceCreationRequestBuilder has two methods: one for each class and instantiates the right XXRequest implementation
   */
  // ResourceCreationRequest(UUID parentId, SubResourcePrototype prototype2) {
  // }

  public AlexandriaResource execute(AlexandriaService service) {
    Log.trace("executing, service=[{}]", service);

    uuid = providedUUID().orElse(UUID.randomUUID());

    String ref = providedRef();
    TentativeAlexandriaProvenance provenance = providedProvenance().orElse(TentativeAlexandriaProvenance.createDefault());
    resourceCreated = service.createOrUpdateResource(uuid, ref, provenance);
    // Log.trace("resource: [{}]", resource);

    return service.readResource(uuid).get();
  }

  public boolean wasExecutedAsIs() {
    final boolean wasExecutedAsIs = providedUUID().isPresent();// && providedProvenance().isPresent();
    Log.trace("wasExecutedAsIs: {}", wasExecutedAsIs);
    return wasExecutedAsIs;
  }

  public boolean newResourceWasCreated() {
    Log.trace("newResourceWasCreated: {}", resourceCreated);
    return resourceCreated;
  }

  public UUID getUUID() {
    return uuid;
  }

  private String providedRef() {
    return requireNonNull(prototype.getRef(), "Required 'ref' field was not validated for being non-null");
  }

  private Optional<UUID> providedUUID() {
    return Optional.ofNullable(prototype.getId()).map(UUIDParam::getValue);
  }

  private Optional<TentativeAlexandriaProvenance> providedProvenance() {
    return prototype.getProvenance().map(ProvenancePrototype::getValue);
  }
}
