package nl.knaw.huygens.alexandria.endpoint.resource;

/*
 * #%L
 * alexandria-main
 * =======
 * Copyright (C) 2015 - 2016 Huygens ING (KNAW)
 * =======
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

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

  private boolean subResourceWasCreated;
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
    Optional<AlexandriaResource> existingSubResource = service.findSubresourceWithSubAndParentId(sub, parentId);
    if (!existingSubResource.isPresent()) {
      service.createSubResource(uuid, parentId, sub, provenance);
      subResourceWasCreated = true;
      return service.readResource(uuid).get();
    } else {
      subResourceWasCreated = false;
      return existingSubResource.get();
    }
  }

  public boolean wasExecutedAsIs() {
    final boolean wasExecutedAsIs = providedUUID().isPresent();// && providedProvenance().isPresent();
    Log.trace("wasExecutedAsIs: {}", wasExecutedAsIs);
    return wasExecutedAsIs;
  }

  public boolean newResourceWasCreated() {
    Log.trace("newResourceWasCreated: {}", subResourceWasCreated);
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

  private Optional<TentativeAlexandriaProvenance> providedProvenance() {
    return prototype.getProvenance().map(ProvenancePrototype::getValue);
  }

}
