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

import static nl.knaw.huygens.alexandria.api.w3c.WebAnnotationConstants.IIIF_PROFILE;
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
    map.put(TechnicalProperties.context, IIIF_PROFILE);
    map.put(TechnicalProperties.id, id);
    map.put(TechnicalProperties.type, getType());
    map.put("label", "some label");
    return map;
  }

  abstract String getType();

}
