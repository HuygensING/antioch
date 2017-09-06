package nl.knaw.huygens.antioch.endpoint.webannotation;

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

import com.github.jsonldjava.core.JsonLdError;
import com.github.jsonldjava.core.JsonLdOptions;
import com.github.jsonldjava.core.JsonLdProcessor;
import com.github.jsonldjava.utils.JsonUtils;
import nl.knaw.huygens.Log;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class WebAnnotationsEndpointTest {

  @Test
  public void testJSONLD() throws IOException, JsonLdError {
    String json = "{"//
      + "\"@context\": \"http://www.w3.org/ns/anno.jsonld\","//
      + "\"id\": \"http://example.org/annotations/anno1\","//
      + "\"type\": \"Annotation\","//
      + "\"created\": \"2015-01-31T12:03:45Z\","//
      + "\"body\": {"//
      + "\"type\": \"TextualBody\","//
      + "\"value\": \"I like this page!\""//
      + "},"//
      + "\"target\": \"http://www.example.com/index.html\"}";
    Map<String, Object> jsonObject = (Map<String, Object>) JsonUtils.fromString(json);
    Log.info("original={}",jsonObject);

    Map<Object, Object> context = new HashMap<>();
    JsonLdOptions options = new JsonLdOptions();
    options.setCompactArrays(true);
    Object expanded = JsonLdProcessor.expand(jsonObject, options);
    Log.info("expanded={}",expanded);

    String context2 = "http://iiif.io/api/presentation/2/context.json";
    Object compacted = JsonLdProcessor.compact(jsonObject, context2, options);
    Log.info("compacted={}",compacted);
  }
}
