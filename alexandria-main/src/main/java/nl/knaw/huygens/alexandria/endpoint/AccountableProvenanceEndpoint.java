package nl.knaw.huygens.alexandria.endpoint;

import java.util.UUID;

import javax.ws.rs.GET;
import javax.ws.rs.core.Response;

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

import io.swagger.annotations.ApiOperation;
import nl.knaw.huygens.alexandria.model.Accountable;
import nl.knaw.huygens.alexandria.model.AlexandriaProvenance;
import nl.knaw.huygens.alexandria.service.AlexandriaService;

public abstract class AccountableProvenanceEndpoint extends JSONEndpoint {
  protected final AlexandriaService service;
  protected final UUID uuid;
  private final LocationBuilder locationBuilder;

  protected AccountableProvenanceEndpoint(AlexandriaService service, //
      final UUIDParam uuidParam, LocationBuilder locationBuilder) {
    this.locationBuilder = locationBuilder;
    this.service = service;
    this.uuid = uuidParam.getValue();
    // Log.trace("resourceService=[{}], uuidParam=[{}]", service, uuidParam);
  }

  protected abstract Accountable getAccountable();

  @GET
  @ApiOperation(value = "get the provenance", response = ProvenanceEntity.class)
  public Response get() {
    AlexandriaProvenance provenance = getAccountable().getProvenance();
    ProvenanceEntity entity = ProvenanceEntity.of(provenance).withLocationBuilder(locationBuilder);
    return ok(entity);
  }

}
