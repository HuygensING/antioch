package nl.knaw.huygens.alexandria.endpoint;

import nl.knaw.huygens.alexandria.api.EndpointPaths;
import nl.knaw.huygens.alexandria.api.model.text.TextEntity;
import nl.knaw.huygens.alexandria.api.model.text.view.ElementViewDefinition;
import nl.knaw.huygens.alexandria.api.model.text.view.TextView;
import nl.knaw.huygens.alexandria.api.model.text.view.TextViewDefinition;
import nl.knaw.huygens.alexandria.api.model.text.view.TextViewEntity;
import nl.knaw.huygens.alexandria.model.AlexandriaResource;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.List;
import java.util.UUID;

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

  public TextViewDefinition createTextViewDefinition(UUID resourceId, TextView textView) {
    TextViewDefinition tvd = new TextViewDefinition()//
      .setDescription(textView.getDescription())//
      ;
    textView.getElementViewMap().forEach((k, v) -> {
      ElementViewDefinition evd = new ElementViewDefinition()//
        .setElementMode(v.getElementMode()) //
        .setAttributeMode(v.getAttributeMode().name());
      v.getPreCondition().ifPresent(precondition -> evd.setWhen(precondition.toString()));
      tvd.setElementViewDefinition(k, evd);
    });
    return tvd;
  }
}
