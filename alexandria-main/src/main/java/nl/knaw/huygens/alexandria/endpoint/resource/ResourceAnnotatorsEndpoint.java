package nl.knaw.huygens.alexandria.endpoint.resource;

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
import nl.knaw.huygens.alexandria.service.AlexandriaService;

public class ResourceAnnotatorsEndpoint extends JSONEndpoint {

  private AlexandriaService service;
  private LocationBuilder locationBuilder;
  private AlexandriaResource resource;

  @Inject
  public ResourceAnnotatorsEndpoint(AlexandriaService service, //
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
    return ok(readExisitingAnnotator(code));
  }

  private Annotator readExisitingAnnotator(String code) {
    return service.readResourceAnnotator(resource.getId(), code).orElseThrow(annotatorNotFoundForCode(code));
  }

  private AnnotatorList readAllAnnotatorsForResource() {
    return service.readResourceAnnotators(resource.getId());
  }

  private Supplier<NotFoundException> annotatorNotFoundForCode(String code) {
    return () -> new NotFoundException("No annotator found with code " + code);
  }

}
