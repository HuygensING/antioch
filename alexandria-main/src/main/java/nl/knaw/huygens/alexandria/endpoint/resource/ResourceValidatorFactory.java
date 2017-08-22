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

  private ResourceValidator validateExistingResource(UUID uuid) {
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
