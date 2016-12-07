package nl.knaw.huygens.alexandria.endpoint.iiif;

import static nl.knaw.huygens.alexandria.api.w3c.WebAnnotationConstants.JSONLD_MEDIATYPE;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.ws.rs.Produces;

import nl.knaw.huygens.alexandria.endpoint.JSONEndpoint;

@Produces(JSONLD_MEDIATYPE)
public abstract class AbstractIIIFEndpoint extends JSONEndpoint {

  private URI id;

  public AbstractIIIFEndpoint(URI id) {
    this.id = id;
  }

  public Map<String, Object> baseMap() {
    Map<String, Object> map = new LinkedHashMap<>();
    map.put(TechnicalProperties.context, "http://iiif.io/api/presentation/2/context.json");
    map.put(TechnicalProperties.id, id);
    map.put(TechnicalProperties.type, getType());
    map.put("label", "some label");
    return map;
  }

  abstract String getType();

}
