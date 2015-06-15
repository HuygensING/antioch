package nl.knaw.huygens.alexandria.endpoint;

import java.util.UUID;

import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import nl.knaw.huygens.Log;
import nl.knaw.huygens.alexandria.model.Accountable;
import nl.knaw.huygens.alexandria.model.AlexandriaProvenance;
import nl.knaw.huygens.alexandria.service.AlexandriaService;

public abstract class AccountableProvenanceEndpoint extends JSONEndpoint {
  protected final AlexandriaService service;
  protected final UUID uuid;
  private final LocationBuilder locationBuilder;

  @Inject
  public AccountableProvenanceEndpoint(AlexandriaService service, //
      @PathParam("uuid") final UUIDParam uuidParam, LocationBuilder locationBuilder) {
    this.locationBuilder = locationBuilder;
    Log.trace("resourceService=[{}], uuidParam=[{}]", service, uuidParam);
    this.service = service;
    this.uuid = uuidParam.getValue();
  }

  protected abstract Accountable getAccountable();

  @GET
  public Response get() {
    AlexandriaProvenance provenance = getAccountable().getProvenance();
    ProvenanceEntity entity = ProvenanceEntity.of(provenance).withLocationBuilder(locationBuilder);
    return Response.ok(entity).build();
  }

  @POST
  public Response postNotSupported() {
    return methodNotImplemented();
  }

  @PUT
  @Path("{uuid}")
  public Response putNotSupported(@PathParam("uuid") final UUIDParam paramId) {
    return methodNotImplemented();
  }

  @DELETE
  @Path("{uuid}")
  public Response deleteNotSupported(@PathParam("uuid") final UUIDParam paramId) {
    return methodNotImplemented();
  }

}
