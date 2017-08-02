package nl.knaw.huygens.alexandria.endpoint.iiif;

/*
 * #%L
 * alexandria-main
 * =======
 * Copyright (C) 2015 - 2017 Huygens ING (KNAW)
 * =======
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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

  private final URI id;

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
