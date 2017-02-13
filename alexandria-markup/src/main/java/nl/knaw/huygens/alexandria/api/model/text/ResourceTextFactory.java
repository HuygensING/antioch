package nl.knaw.huygens.alexandria.api.model.text;

/*
 * #%L
 * alexandria-main
 * =======
 * Copyright (C) 2015 - 2017 Huygens ING (KNAW)
 * =======
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import nl.knaw.huygens.alexandria.api.EndpointPaths;
import nl.knaw.huygens.alexandria.api.model.text.view.TextView;
import nl.knaw.huygens.alexandria.api.model.text.view.TextViewEntity;
import nl.knaw.huygens.alexandria.endpoint.LocationBuilder;
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

}
