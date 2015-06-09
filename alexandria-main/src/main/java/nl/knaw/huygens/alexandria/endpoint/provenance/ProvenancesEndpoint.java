package nl.knaw.huygens.alexandria.endpoint.provenance;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import nl.knaw.huygens.alexandria.endpoint.EndpointPaths;
import nl.knaw.huygens.alexandria.endpoint.JSONEndpoint;
import nl.knaw.huygens.alexandria.endpoint.UUIDParam;
import nl.knaw.huygens.alexandria.model.AlexandriaProvenance;
import nl.knaw.huygens.alexandria.service.AlexandriaService;

@Path(EndpointPaths.PROVENANCES)
public class ProvenancesEndpoint extends JSONEndpoint {
  private final AlexandriaService service;
  private final ProvenanceEntityBuilder entityBuilder;

  @Inject
  public ProvenancesEndpoint(AlexandriaService service, //
      ProvenanceEntityBuilder entityBuilder) {
    this.service = service;
    this.entityBuilder = entityBuilder;
  }

  @GET
  @Path("{uuid}")
  public Response readProvenance(@PathParam("uuid") UUIDParam uuidParam) {
    final AlexandriaProvenance provenance = service.readProvenance(uuidParam.getValue());
    final ProvenanceEntity entity = entityBuilder.build(provenance);
    return Response.ok(entity).build();
  }

}
