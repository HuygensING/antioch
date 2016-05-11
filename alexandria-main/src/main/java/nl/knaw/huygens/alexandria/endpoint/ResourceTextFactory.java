package nl.knaw.huygens.alexandria.endpoint;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.core.UriBuilder;

import nl.knaw.huygens.alexandria.api.EndpointPaths;
import nl.knaw.huygens.alexandria.api.model.TextEntity;
import nl.knaw.huygens.alexandria.api.model.TextView;
import nl.knaw.huygens.alexandria.api.model.TextViewEntity;
import nl.knaw.huygens.alexandria.model.AlexandriaResource;

@Singleton
public class ResourceTextFactory {
  private LocationBuilder locationBuilder;

  @Inject
  public ResourceTextFactory(LocationBuilder locationBuilder) {
    this.locationBuilder = locationBuilder;
  }

  public TextEntity createTextEntity(UUID resourceId, List<TextViewEntity> textViews) {
    URI xmlURI = xmlURI(resourceId);
    URI dotURI = locationBuilder.locationOf(AlexandriaResource.class, resourceId, EndpointPaths.TEXT, "dot");
    TextEntity textEntity = new TextEntity();
    textEntity.setXmlURI(xmlURI);
    textEntity.setDotURI(dotURI);
    textEntity.setTextViews(textViews);
    return textEntity;
  }

  public TextViewEntity createTextViewEntity(UUID resourceId, TextView textView) {
    UUID viewResourceId = textView.getTextViewDefiningResourceId();
    String viewId = textView.getName();
    URI uri = xmlURI(resourceId);
    URI xmlViewURI = UriBuilder.fromUri(uri).queryParam("view", viewId).build();
    URI definitionURI = locationBuilder.locationOf(AlexandriaResource.class, viewResourceId, EndpointPaths.TEXT, EndpointPaths.TEXTVIEWS, viewId);
    TextViewEntity textViewEntity = new TextViewEntity();
    textViewEntity.setId(viewId);
    textViewEntity.setDefinitionURI(definitionURI);
    textViewEntity.setXmlURI(xmlViewURI);
    return textViewEntity;
  }

  private URI xmlURI(UUID resourceId) {
    return locationBuilder.locationOf(AlexandriaResource.class, resourceId, EndpointPaths.TEXT, "xml");
  }

}
