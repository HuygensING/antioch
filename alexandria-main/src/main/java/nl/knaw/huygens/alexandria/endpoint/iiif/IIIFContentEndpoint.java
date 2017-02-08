package nl.knaw.huygens.alexandria.endpoint.iiif;

import java.net.URI;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.core.Response;

import nl.knaw.huygens.alexandria.service.AlexandriaService;

public class IIIFContentEndpoint extends AbstractIIIFEndpoint {

  private AlexandriaService service;
  private String name;
  private String identifier;
  private String format;

  public IIIFContentEndpoint(String identifier, String name, String format, AlexandriaService service, URI id) {
    super(id);
    this.identifier = identifier;
    this.name = name;
    this.format = format;
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
    return "sc:Content";
  }

}
