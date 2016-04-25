package nl.knaw.huygens.alexandria.textgraph;

import java.util.UUID;

import com.google.common.base.Joiner;

import nl.knaw.huygens.alexandria.api.EndpointPaths;
import nl.knaw.huygens.alexandria.endpoint.LocationBuilder;
import nl.knaw.huygens.alexandria.model.AlexandriaResource;
import nl.knaw.huygens.alexandria.service.AlexandriaService;

public class TextGraphImportTask implements Runnable {

  private AlexandriaService service;
  private LocationBuilder locationBuilder;
  private String xml;
  private AlexandriaResource resource;
  private TextGraphImportStatus status;
  private UUID resourceId;

  public TextGraphImportTask(AlexandriaService service, LocationBuilder locationBuilder, String xml, AlexandriaResource resource) {
    this.service = service;
    this.locationBuilder = locationBuilder;
    this.xml = xml;
    this.resource = resource;
    this.resourceId = resource.getId();
    this.status = new TextGraphImportStatus();
  }

  public TextGraphImportStatus getStatus() {
    return status;
  }

  @Override
  public void run() {
    status.setStarted();
    try {
      ParseResult result = TextGraphUtil.parse(xml);
      boolean success = service.storeTextGraph(resourceId, result);
      if (success) {
        status.setTextURI(locationBuilder.locationOf(resource, EndpointPaths.TEXT, "xml"));
      } else {
        status.getValidationErrors().add("textgraph store failed");
      }
    } catch (Exception e) {
      e.printStackTrace();
      status.getValidationErrors().add("Exception thrown: " + e);
      status.getValidationErrors().add("Exception message: " + e.getMessage());
      status.getValidationErrors().add("Stacktrace: " + Joiner.on("\n").join(e.getStackTrace()));
    }
    status.setDone();
  }

}
