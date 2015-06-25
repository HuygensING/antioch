package nl.knaw.huygens.alexandria.endpoint;

import io.swagger.annotations.ApiOperation;

import java.util.UUID;

import javax.ws.rs.GET;
import javax.ws.rs.core.Response;

import nl.knaw.huygens.Log;
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
    Log.trace("resourceService=[{}], uuidParam=[{}]", service, uuidParam);
  }

  protected abstract Accountable getAccountable();

  @GET
  @ApiOperation(value = "get the provenance", response = ProvenanceEntity.class)
  public Response get() {
    AlexandriaProvenance provenance = getAccountable().getProvenance();
    ProvenanceEntity entity = ProvenanceEntity.of(provenance).withLocationBuilder(locationBuilder);
    return Response.ok(entity).build();
  }

}
