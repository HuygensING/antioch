package nl.knaw.huygens.alexandria.endpoint.iiif;

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
