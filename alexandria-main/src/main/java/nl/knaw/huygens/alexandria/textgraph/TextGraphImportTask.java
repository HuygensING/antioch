package nl.knaw.huygens.alexandria.textgraph;

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

import java.util.UUID;

import com.google.common.base.Joiner;

import nl.knaw.huygens.alexandria.api.EndpointPaths;
import nl.knaw.huygens.alexandria.api.model.text.TextImportStatus;
import nl.knaw.huygens.alexandria.endpoint.LocationBuilder;
import nl.knaw.huygens.alexandria.model.AlexandriaResource;
import nl.knaw.huygens.alexandria.service.AlexandriaService;

public class TextGraphImportTask implements Runnable {

  private AlexandriaService service;
  private LocationBuilder locationBuilder;
  private String xml;
  private AlexandriaResource resource;
  private TextImportStatus status;
  private UUID resourceId;

  public TextGraphImportTask(AlexandriaService service, LocationBuilder locationBuilder, String xml, AlexandriaResource resource) {
    this.service = service;
    this.locationBuilder = locationBuilder;
    this.xml = xml;
    this.resource = resource;
    this.resourceId = resource.getId();
    this.status = new TextImportStatus();
  }

  public TextImportStatus getStatus() {
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
