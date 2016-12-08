package nl.knaw.huygens.alexandria.endpoint.iiif;

import java.net.URI;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.core.Response;

import nl.knaw.huygens.alexandria.service.AlexandriaService;

public class IIIFAnnotationEndpoint extends AbstractIIIFEndpoint {

  private AlexandriaService service;
  private String name;
  private String identifier;

  public IIIFAnnotationEndpoint(String identifier, String name, AlexandriaService service, URI id) {
    super(id);
    this.identifier = identifier;
    this.name = name;
    this.service = service;
  }

  @GET
  public Response get() {
    return notImplemented(dummySequence());
  }

  private Map<String, Object> dummySequence() {
    Map<String, Object> dummy = baseMap();
    return dummy;
  }

  @Override
  String getType() {
    return "oa:Annotation";
  }

}
