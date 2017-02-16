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

import java.net.URI;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.swagger.annotations.ApiOperation;
import nl.knaw.huygens.alexandria.api.EndpointPaths;
import nl.knaw.huygens.alexandria.api.model.Annotator;
import nl.knaw.huygens.alexandria.api.model.AnnotatorList;
import nl.knaw.huygens.alexandria.endpoint.JSONEndpoint;
import nl.knaw.huygens.alexandria.endpoint.LocationBuilder;
import nl.knaw.huygens.alexandria.endpoint.UUIDParam;
import nl.knaw.huygens.alexandria.exception.ConflictException;
import nl.knaw.huygens.alexandria.exception.NotFoundException;
import nl.knaw.huygens.alexandria.model.AlexandriaResource;
import nl.knaw.huygens.alexandria.service.MarkupService;

public class ResourceAnnotatorsEndpoint extends JSONEndpoint {

  private MarkupService service;
  private LocationBuilder locationBuilder;
  private AlexandriaResource resource;

  @Inject
  public ResourceAnnotatorsEndpoint(MarkupService service, //
      LocationBuilder locationBuilder, //
      ResourceValidatorFactory validatorFactory, //
      @PathParam("uuid") final UUIDParam uuidParam) {
    this.service = service;
    this.locationBuilder = locationBuilder;
    this.resource = validatorFactory.validateExistingResource(uuidParam).notTentative().get();
  }

  @GET
  @ApiOperation("get annotators")
  public Response getAnnotators() {
    return ok(readAllAnnotatorsForResource());
  }

  @PUT
  @Path("{code}")
  @Consumes(MediaType.APPLICATION_JSON)
  @ApiOperation("set annotator")
  public Response setAnnotator(@PathParam("code") final String code, //
      @NotNull @ValidAnnotator Annotator annotator) {
    annotator.setCode(code);
    verifyAnnotatorCodeNotInUseByAncestorResource(code);
    Optional<Annotator> existingAnnotator = service.readResourceAnnotator(resource.getId(), code);
    service.setResourceAnnotator(resource.getId(), annotator);
    if (existingAnnotator.isPresent()) {
      return noContent();
    }

    URI uri = locationBuilder.locationOf(resource, EndpointPaths.ANNOTATORS, code);
    return created(uri);
  }

  private void verifyAnnotatorCodeNotInUseByAncestorResource(String code) {
    UUID resourceUUID = resource.getId();
    AnnotatorList resourceAnnotators = service.readResourceAnnotators(resourceUUID);
    Optional<Annotator> ancestorAnnotator = resourceAnnotators.stream()//
        .filter(a -> a.getCode().equals(code))//
        .filter(a -> !a.getResourceURI().toString().contains(resourceUUID.toString()))//
        .findAny();
    if (ancestorAnnotator.isPresent()) {
      throw new ConflictException("Annotator '" + code + "' already defined in (sub)resource chain.");
    }

  }

  @GET
  @Path("{code}")
  @ApiOperation("get annotator")
  public Response getAnnotator(@PathParam("code") final String code) {
    return ok(readExistingAnnotator(code));
  }

  private Annotator readExistingAnnotator(String code) {
    return service.readResourceAnnotator(resource.getId(), code).orElseThrow(annotatorNotFoundForCode(code));
  }

  private AnnotatorList readAllAnnotatorsForResource() {
    return service.readResourceAnnotators(resource.getId());
  }

  private Supplier<NotFoundException> annotatorNotFoundForCode(String code) {
    return () -> new NotFoundException("No annotator found with code " + code);
  }

}
