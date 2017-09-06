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

import javax.inject.Inject;
import javax.ws.rs.PathParam;

import io.swagger.annotations.Api;
import nl.knaw.huygens.antioch.endpoint.AccountableProvenanceEndpoint;
import nl.knaw.huygens.antioch.endpoint.LocationBuilder;
import nl.knaw.huygens.antioch.endpoint.UUIDParam;
import nl.knaw.huygens.antioch.model.Accountable;
import nl.knaw.huygens.antioch.service.AntiochService;

@Api("resources")
public class ResourceProvenanceEndpoint extends AccountableProvenanceEndpoint {
  @Inject
  public ResourceProvenanceEndpoint(AntiochService service,  //
      @PathParam("uuid") final UUIDParam uuidParam, LocationBuilder locationBuilder) {
    super(service, uuidParam, locationBuilder);
  }

  @Override
  protected Accountable getAccountable() {
    return service.readResource(uuid).orElseThrow(ResourceValidatorFactory.resourceNotFoundForId(uuid));
  }

}
