package nl.knaw.huygens.antioch.endpoint.iiif;

/*
 * #%L
 * antioch-main
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

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.core.Response;

import nl.knaw.huygens.antioch.service.AntiochService;

public class IIIFManifestEndpoint extends AbstractIIIFEndpoint {

  public IIIFManifestEndpoint(String identifier, AntiochService service, URI id) {
    super(id);
    String identifier1 = identifier;
    AntiochService service1 = service;
  }

  @GET
  public Response get() {
    return notImplemented(dummySequence());
  }

  private Map<String, Object> dummySequence() {
    Map<String, Object> dummy = baseMap();
    List<Map<String, Object>> sequences = new ArrayList<>();
    dummy.put("sequences", sequences); // required
    return dummy;
  }

  @Override
  String getType() {
    return "sc:Manifest";
  }

}
