package nl.knaw.huygens.alexandria.endpoint.iiif;

import static nl.knaw.huygens.alexandria.api.w3c.WebAnnotationConstants.JSONLD_MEDIATYPE;

import java.io.IOException;
import java.net.URI;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.core.Response;

import com.github.jsonldjava.utils.JsonUtils;
import com.google.common.collect.Maps;

import nl.knaw.huygens.alexandria.api.model.iiif.IIIFAnnotationList;
import nl.knaw.huygens.alexandria.config.AlexandriaConfiguration;
import nl.knaw.huygens.alexandria.endpoint.webannotation.WebAnnotation;
import nl.knaw.huygens.alexandria.endpoint.webannotation.WebAnnotationService;
import nl.knaw.huygens.alexandria.service.AlexandriaService;

public class IIIFAnnotationListEndpoint extends AbstractIIIFEndpoint {

  private String name;
  private String identifier;
  private WebAnnotationService webAnnotationService;

  public IIIFAnnotationListEndpoint(String identifier, String name, AlexandriaService service, AlexandriaConfiguration config, URI id) {
    super(id);
    this.identifier = identifier;
    this.name = name;
    this.webAnnotationService = new WebAnnotationService(service, config);
  }

  @GET
  public Response get() {
    return notImplemented(dummySequence());
  }

  @POST
  @Consumes(JSONLD_MEDIATYPE)
  public Response postAnnotationList(IIIFAnnotationList annotationList) {
    Map<String, Object> otherProperties = annotationList.getOtherProperties();
    String context = (String) otherProperties.get("@context");
    Map<String, Object> processedList = Maps.newHashMap(otherProperties);
    List<Map<String, Object>> resources = new ArrayList<>(annotationList.getResources().size());
    annotationList.getResources().forEach(prototype -> {
      prototype.setCreated(Instant.now().toString());
      prototype.getVariablePart().put("@context", context);
      WebAnnotation webAnnotation = webAnnotationService.validateAndStore(prototype);
      try {
        Map<String, Object> map = (Map<String, Object>) JsonUtils.fromString(webAnnotation.json());
        resources.add(map);
      } catch (IOException e) {
        e.printStackTrace();
      }
    });
    processedList.put("resources", resources);
    return ok(processedList);
  }

  private Map<String, Object> dummySequence() {
    Map<String, Object> dummy = baseMap();
    return dummy;
  }

  @Override
  String getType() {
    return "sc:AnnotationList";
  }

}
