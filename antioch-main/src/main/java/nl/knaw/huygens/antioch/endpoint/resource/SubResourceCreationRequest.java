package nl.knaw.huygens.antioch.endpoint.resource;

/*
 * #%L
 * antioch-main
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

import nl.knaw.huygens.antioch.api.model.AntiochState;
import nl.knaw.huygens.antioch.endpoint.CreationRequest;
import nl.knaw.huygens.antioch.endpoint.ProvenancePrototype;
import nl.knaw.huygens.antioch.endpoint.UUIDParam;
import nl.knaw.huygens.antioch.model.AntiochResource;
import nl.knaw.huygens.antioch.model.TentativeAntiochProvenance;
import nl.knaw.huygens.antioch.service.AntiochService;

class SubResourceCreationRequest implements CreationRequest<AntiochResource> {

  private boolean subResourceWasCreated;
  private final UUID parentId;
  private final SubResourcePrototype prototype;
  private UUID uuid;

  public SubResourceCreationRequest(UUID parentId, SubResourcePrototype prototype) {
    this.parentId = parentId;
    this.prototype = prototype;
  }

  @Override
  public AntiochResource execute(AntiochService service) {
    // Log.trace("executing, service=[{}]", service);

    uuid = providedUUID().orElse(UUID.randomUUID());

    String sub = providedSub();
    TentativeAntiochProvenance provenance = providedProvenance().orElse(TentativeAntiochProvenance.createDefault());
    Optional<AntiochResource> existingSubResourceByRef = service.findSubresourceWithSubAndParentId(sub, parentId);
    Optional<AntiochResource> existingSubResourceByUUID = service.readResource(uuid);
    if (!existingSubResourceByRef.isPresent() && !existingSubResourceByUUID.isPresent()) {
      service.createSubResource(uuid, parentId, sub, provenance);
      subResourceWasCreated = true;
      if (prototype.getState().equals(AntiochState.CONFIRMED)) {
        service.confirmResource(uuid);
      }
      return service.readResource(uuid).get();
    } else if (existingSubResourceByRef.isPresent()) {
      subResourceWasCreated = false;
      return existingSubResourceByRef.get();
    }
    subResourceWasCreated = false;
    service.createOrUpdateResource(uuid, sub, provenance, prototype.getState());
    return service.readResource(uuid).get();
  }

  public boolean wasExecutedAsIs() {
    final boolean wasExecutedAsIs = providedUUID().isPresent();// && providedProvenance().isPresent();
    // Log.trace("wasExecutedAsIs: {}", wasExecutedAsIs);
    return wasExecutedAsIs;
  }

  public boolean newResourceWasCreated() {
    // Log.trace("newResourceWasCreated: {}", subResourceWasCreated);
    return subResourceWasCreated;
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

  private Optional<TentativeAntiochProvenance> providedProvenance() {
    return prototype.getProvenance().map(ProvenancePrototype::getValue);
  }

}
