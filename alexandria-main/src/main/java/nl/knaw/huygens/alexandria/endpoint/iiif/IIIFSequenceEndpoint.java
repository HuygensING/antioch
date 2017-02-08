package nl.knaw.huygens.alexandria.endpoint.iiif;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.core.Response;

import nl.knaw.huygens.alexandria.service.AlexandriaService;

public class IIIFSequenceEndpoint extends AbstractIIIFEndpoint {

  private AlexandriaService service;
  private String name;
  private String identifier;

  public IIIFSequenceEndpoint(String identifier, String name, AlexandriaService service, URI id) {
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
    List<Map<String, Object>> canvases = new ArrayList<>();
    dummy.put("canvases", canvases); // required
    return dummy;
  }

  @Override
  String getType() {
    return "sc:Sequence";
  }

}
