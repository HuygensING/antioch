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

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicBoolean;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;

import nl.knaw.huygens.alexandria.endpoint.webannotation.WebAnnotationService;
import nl.knaw.huygens.alexandria.exception.BadRequestException;

public class AnnotationListHandler {

  private JsonParser jParser;
  private String context;
  private final Deque<String> deque = new ConcurrentLinkedDeque<>();
  private final AtomicBoolean firstField = new AtomicBoolean(true);
  private final AtomicBoolean inResources = new AtomicBoolean(false);
  private final WebAnnotationService webAnnotationService;

  public AnnotationListHandler(InputStream inputStream, WebAnnotationService webAnnotationService) {
    this.webAnnotationService = webAnnotationService;
    try {
      jParser = new JsonFactory().createParser(inputStream);
      deque.add("{");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public String getNextString() {
    try {
      if (inResources.get()) {
        handleNextResource();

      } else {
        handleRootFields();
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return deque.isEmpty() ? null : deque.pop();
  }

  private void handleRootFields() throws IOException {
    if (jParser.nextToken() != JsonToken.END_OBJECT) {
      String fieldname = jParser.getCurrentName();
      if (fieldname != null) {
        if (!firstField.get()) {
          deque.add(",");
        }
        firstField.set(false);

        switch (fieldname) {
        case "@context":
          jParser.nextToken();
          context = jParser.getText(); // we'll be needing this later
          deque.add("\"@context\":\"" + context + "\"");
          break;

        case "resources":
          if (context == null) {
            throw new BadRequestException("Missing @context field, should be defined at the start of the json payload.");
          }
          deque.add("\"resources\":[");
          jParser.nextToken(); // "["
          // parse each resource
          firstField.set(true);
          inResources.set(true);
          break;

        default:
          jParser.nextToken();
          deque.add("\"" + fieldname + "\":");
          JsonToken currentToken = jParser.currentToken();
          if (currentToken.isStructStart()) {
            deque.add(new ObjectMapper().readTree(jParser).toString());
          } else if (currentToken.isNumeric()) {
            deque.add(jParser.getValueAsString());
          } else {
            deque.add("\"" + jParser.getText() + "\"");
          }
          break;
        }
      }

    } else {
      close();
    }
  }

  private void handleNextResource() throws IOException {
    if (jParser.nextToken() != JsonToken.END_ARRAY) {
      if (!firstField.get()) {
        deque.add(",");
      }
      firstField.set(false);
      ObjectNode annotationNode = new ObjectMapper().readTree(jParser);
      String created = Instant.now().toString();
      annotationNode.set("http://purl.org/dc/terms/created", new TextNode(created));
      annotationNode.set("@context", new TextNode(context));
      ObjectNode storedAnnotation = webAnnotationService.validateAndStore(annotationNode);
      deque.add(new ObjectMapper().writeValueAsString(storedAnnotation));

    } else {
      deque.add("]");
      inResources.set(false);
    }
  }

  private void close() {
    try {
      jParser.close();
      deque.add("}");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
