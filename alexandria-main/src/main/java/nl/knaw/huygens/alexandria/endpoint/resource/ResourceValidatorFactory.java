package nl.knaw.huygens.alexandria.endpoint.resource;

/*
 * #%L
 * alexandria-main
 * =======
 * Copyright (C) 2015 - 2017 Huygens ING (KNAW)
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

import java.util.UUID;
import java.util.function.Supplier;

import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;

import nl.knaw.huygens.alexandria.api.model.AlexandriaState;
import nl.knaw.huygens.alexandria.endpoint.UUIDParam;
import nl.knaw.huygens.alexandria.exception.ConflictException;
import nl.knaw.huygens.alexandria.exception.NotFoundException;
import nl.knaw.huygens.alexandria.exception.TentativeObjectException;
import nl.knaw.huygens.alexandria.model.AlexandriaResource;
import nl.knaw.huygens.alexandria.service.AlexandriaService;

public class ResourceValidatorFactory {
  private final AlexandriaService service;

  @Inject
  public ResourceValidatorFactory(AlexandriaService service) {
    this.service = service;
  }

  public static Supplier<NotFoundException> resourceNotFoundForId(Object id) {
    return () -> new NotFoundException("No resource found with id " + id);
  }

  public static WebApplicationException resourceIsTentativeException(UUID uuid) {
    return new TentativeObjectException("resource " + uuid + " is tentative, please confirm first");
  }

  public ResourceValidator validateExistingResource(UUIDParam uuidParam) {
    return validateExistingResource(uuidParam.getValue());
  }

  public ResourceValidator validateExistingResource(UUID uuid) {
    return new ResourceValidator(uuid);
  }

  class ResourceValidator {
    private final AlexandriaResource resource;

    public ResourceValidator(UUID resourceId) {
      this.resource = service.readResource(resourceId).orElseThrow(resourceNotFoundForId(resourceId));
    }

    public AlexandriaResource get() {
      return resource;
    }

    public ResourceValidator notTentative() {
      if (resource.getState() == AlexandriaState.TENTATIVE) {
        throw resourceIsTentativeException(resource.getId());
      }
      return this;
    }

    public ResourceValidator inState(AlexandriaState requiredState) {
      final AlexandriaState actualState = resource.getState();
      if (actualState != requiredState) {
        throw new ConflictException("Object not in state " + requiredState + " but in state " + actualState);
      }

      return this;
    }

    public ResourceValidator notInState(AlexandriaState requiredState) {
      final AlexandriaState actualState = resource.getState();
      if (actualState == requiredState) {
        throw new ConflictException("Object should not be in state " + requiredState + " but is.");
      }

      return this;
    }

    public ResourceValidator hasText() {
      if (!resource.hasText()) {
        throw new ConflictException("Resource should have a text, but doesn't.");
      }
      return this;
    }
  }

}
