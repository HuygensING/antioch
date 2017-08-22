package nl.knaw.huygens.alexandria.endpoint.resource;

/*
 * #%L
 * alexandria-main
 * =======
 * Copyright (C) 2015 - 2017 Huygens ING (KNAW)
 * =======
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import static java.util.Objects.requireNonNull;

import java.util.Optional;
import java.util.UUID;

import nl.knaw.huygens.alexandria.endpoint.CreationRequest;
import nl.knaw.huygens.alexandria.endpoint.ProvenancePrototype;
import nl.knaw.huygens.alexandria.endpoint.UUIDParam;
import nl.knaw.huygens.alexandria.model.AlexandriaResource;
import nl.knaw.huygens.alexandria.model.TentativeAlexandriaProvenance;
import nl.knaw.huygens.alexandria.service.AlexandriaService;

class ResourceCreationRequest implements CreationRequest<AlexandriaResource> {

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

  @Override
  public AlexandriaResource execute(AlexandriaService service) {
    // Log.trace("executing, service=[{}]", service);

    uuid = providedUUID().orElse(UUID.randomUUID());

    String ref = providedRef();
    TentativeAlexandriaProvenance provenance = providedProvenance().orElse(TentativeAlexandriaProvenance.createDefault());
    resourceCreated = service.createOrUpdateResource(uuid, ref, provenance, prototype.getState());
    // Log.trace("resource: [{}]", resource);

    return service.readResource(uuid).get();
  }

  public boolean wasExecutedAsIs() {
    final boolean wasExecutedAsIs = providedUUID().isPresent();// && providedProvenance().isPresent();
    // Log.trace("wasExecutedAsIs: {}", wasExecutedAsIs);
    return wasExecutedAsIs;
  }

  public boolean newResourceWasCreated() {
    // Log.trace("newResourceWasCreated: {}", resourceCreated);
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
