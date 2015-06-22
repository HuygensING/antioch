package nl.knaw.huygens.alexandria.endpoint.annotation;

import javax.inject.Inject;
import javax.ws.rs.PathParam;

import nl.knaw.huygens.alexandria.endpoint.AccountableProvenanceEndpoint;
import nl.knaw.huygens.alexandria.endpoint.LocationBuilder;
import nl.knaw.huygens.alexandria.endpoint.UUIDParam;
import nl.knaw.huygens.alexandria.model.Accountable;
import nl.knaw.huygens.alexandria.service.AlexandriaService;

public class AnnotationProvenanceEndpoint extends AccountableProvenanceEndpoint {
  @Inject
  public AnnotationProvenanceEndpoint(AlexandriaService service, //
      @PathParam("uuid") final UUIDParam uuidParam, LocationBuilder locationBuilder) {
    super(service, uuidParam, locationBuilder);
  }

  @Override
  protected Accountable getAccountable() {
    return service.readAnnotation(uuid).get();
  };

}
