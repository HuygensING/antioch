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

import java.util.UUID;
import java.util.function.Supplier;

import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;

import nl.knaw.huygens.antioch.api.model.AntiochState;
import nl.knaw.huygens.antioch.endpoint.UUIDParam;
import nl.knaw.huygens.antioch.exception.ConflictException;
import nl.knaw.huygens.antioch.exception.NotFoundException;
import nl.knaw.huygens.antioch.exception.TentativeObjectException;
import nl.knaw.huygens.antioch.model.AntiochResource;
import nl.knaw.huygens.antioch.service.AntiochService;

public class ResourceValidatorFactory {
  private final AntiochService service;

  @Inject
  public ResourceValidatorFactory(AntiochService service) {
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

  private ResourceValidator validateExistingResource(UUID uuid) {
    return new ResourceValidator(uuid);
  }

  class ResourceValidator {
    private final AntiochResource resource;

    public ResourceValidator(UUID resourceId) {
      this.resource = service.readResource(resourceId).orElseThrow(resourceNotFoundForId(resourceId));
    }

    public AntiochResource get() {
      return resource;
    }

    public ResourceValidator notTentative() {
      if (resource.getState() == AntiochState.TENTATIVE) {
        throw resourceIsTentativeException(resource.getId());
      }
      return this;
    }

    public ResourceValidator inState(AntiochState requiredState) {
      final AntiochState actualState = resource.getState();
      if (actualState != requiredState) {
        throw new ConflictException("Object not in state " + requiredState + " but in state " + actualState);
      }

      return this;
    }

    public ResourceValidator notInState(AntiochState requiredState) {
      final AntiochState actualState = resource.getState();
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
