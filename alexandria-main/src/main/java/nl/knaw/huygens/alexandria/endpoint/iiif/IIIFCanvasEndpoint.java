package nl.knaw.huygens.alexandria.endpoint.iiif;

import java.net.URI;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.core.Response;

import nl.knaw.huygens.alexandria.service.AlexandriaService;

public class IIIFCanvasEndpoint extends AbstractIIIFEndpoint {

  private AlexandriaService service;
  private String name;
  private String identifier;

  public IIIFCanvasEndpoint(String identifier, String name, AlexandriaService service, URI id) {
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
    dummy.put("height", 1); // required
    dummy.put("width", 1);// required
    return dummy;
  }

  @Override
  String getType() {
    return "sc:Canvas";
  }

}
