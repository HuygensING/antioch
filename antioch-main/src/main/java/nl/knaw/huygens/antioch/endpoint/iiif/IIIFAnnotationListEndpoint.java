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

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.github.jsonldjava.utils.JsonUtils;
import com.google.common.collect.Maps;
import nl.knaw.huygens.antioch.api.EndpointPaths;
import nl.knaw.huygens.antioch.api.model.ProcessStatusMap;
import nl.knaw.huygens.antioch.api.model.iiif.IIIFAnnotationList;
import nl.knaw.huygens.antioch.config.AntiochConfiguration;
import nl.knaw.huygens.antioch.endpoint.webannotation.WebAnnotation;
import nl.knaw.huygens.antioch.endpoint.webannotation.WebAnnotationService;
import nl.knaw.huygens.antioch.exception.BadRequestException;
import nl.knaw.huygens.antioch.exception.NotFoundException;
import nl.knaw.huygens.antioch.service.AntiochService;
import org.glassfish.jersey.server.ChunkedOutput;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

import static nl.knaw.huygens.antioch.api.w3c.WebAnnotationConstants.JSONLD_MEDIATYPE;

public class IIIFAnnotationListEndpoint extends AbstractIIIFEndpoint {

  private final String name;
  private final AntiochConfiguration config;
  private final String identifier;
  private final WebAnnotationService webAnnotationService;
  private final ProcessStatusMap<AnnotationListImportStatus> taskStatusMap;
  private final ExecutorService executorService;

  public IIIFAnnotationListEndpoint(String identifier,
                                    String name,
                                    AntiochService service,
                                    AntiochConfiguration config,
                                    URI id,
                                    ProcessStatusMap<AnnotationListImportStatus> taskStatusMap,
                                    ExecutorService executorService) {
    super(id);
    this.identifier = identifier;
    this.name = name;
    this.config = config;
    this.taskStatusMap = taskStatusMap;
    this.executorService = executorService;
    this.webAnnotationService = new WebAnnotationService(service, config);
    String listId = identifier + ":" + name;
  }

  @GET
  public Response get() {
    return notImplemented(dummySequence());
  }

  @POST
  // @Path("streaming")
  @Consumes(JSONLD_MEDIATYPE)
  public Response postAnnotationListStreaming(InputStream inputStream) throws IOException {
    StreamingOutput outStream = os -> {
      JsonFactory jsonFactory = new JsonFactory();
      JsonParser jParser = jsonFactory.createParser(inputStream);
      JsonGenerator jGenerator = jsonFactory.createGenerator(os);
      String context = null;
      jGenerator.writeStartObject();
      while (jParser.nextToken() != JsonToken.END_OBJECT) {
        String fieldname = jParser.getCurrentName();
        // Log.info("fieldname={}", fieldname);
        if ("@context".equals(fieldname)) {
          jParser.nextToken();
          context = jParser.getText();
          jGenerator.writeFieldName("@context");
          jGenerator.writeString(context);

        } else if ("resources".equals(fieldname)) {
          if (context == null) {
            throw new BadRequestException("Missing @context field, should be defined at the start of the json payload.");
          }
          parseResources(jParser, jGenerator, context);

        } else if (fieldname != null) {
          jParser.nextToken();
          jGenerator.writeFieldName(fieldname);
          JsonToken currentToken = jParser.currentToken();
          if (currentToken.isStructStart()) {
            jGenerator.writeRaw(new ObjectMapper().readTree(jParser).toString());
          } else if (currentToken.isNumeric()) {
            jGenerator.writeNumber(jParser.getValueAsString());
          } else {
            jGenerator.writeString(jParser.getValueAsString());
          }
        }
      }
      jParser.close();

      jGenerator.writeEndObject();
      jGenerator.flush();
      jGenerator.close();
    };
    return Response.ok(outStream).build();
  }

  @POST
  @Path("chunked")
  @Consumes(JSONLD_MEDIATYPE)
  public ChunkedOutput<String> postAnnotationListChunked(InputStream inputStream) throws IOException {
    final ChunkedOutput<String> output = new ChunkedOutput<>(String.class);
    AnnotationListHandler alh = new AnnotationListHandler(inputStream, webAnnotationService);
    new Thread(() -> {
      try {
        String chunk;
        while ((chunk = alh.getNextString()) != null) {
          output.write(chunk);
        }
      } catch (IOException e) {
        e.printStackTrace();
        throw new RuntimeException(e);
      } finally {
        try {
          output.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }).start();
    return output;
  }

  @POST
  @Path("oldway")
  @Consumes(JSONLD_MEDIATYPE)
  public Response postAnnotationList(IIIFAnnotationList annotationList) {
    Map<String, Object> otherProperties = annotationList.getOtherProperties();
    String context = annotationList.getContext();
    Map<String, Object> processedList = Maps.newHashMap(otherProperties);
    List<Map<String, Object>> resources = new ArrayList<>(annotationList.getResources().size());
    annotationList.getResources().forEach(prototype -> {
      prototype.setCreated(Instant.now().toString());
      prototype.getVariablePart().put("@context", context);
      WebAnnotation webAnnotation = webAnnotationService.validateAndStore(prototype);
      try {
        Map<String, Object> map = (Map<String, Object>) JsonUtils.fromString(webAnnotation.json());
        resources.add(map);
      } catch (IOException e) {
        e.printStackTrace();
      }
    });
    processedList.put("resources", resources);
    return ok(processedList);
  }

  @POST
  @Path("async")
  @Consumes(JSONLD_MEDIATYPE)
  public Response postAnnotationListAsynchronously(IIIFAnnotationList annotationList) {
    UUID statusId = startAnnotationListImport(annotationList);
    URI statusURI = UriBuilder.fromUri(config.getBaseURI())
            .path(EndpointPaths.IIIF)
            .path(identifier)
            .path("list")
            .path(name)
            .path("async")
            .path("status")
            .path(statusId.toString())
            .build();
    return Response.accepted()//
            .location(statusURI)//
            .build();
  }

  private UUID startAnnotationListImport(IIIFAnnotationList annotationList) {
    AnnotationListImportTask task = new AnnotationListImportTask(annotationList, webAnnotationService);
    UUID statusUUID = UUID.randomUUID();
    taskStatusMap.put(statusUUID, task.getStatus());
    runTask(task);
    return statusUUID;


  }

  private void runTask(Runnable task) {
    if (config.asynchronousEndpointsAllowed()) {
      executorService.execute(task);
    } else {
      // For now, for the acceptance tests.
      task.run();
    }
  }

  @GET
  @Path("async/status/{uuid}")
  public Response getAnnotationListImportStatus(@PathParam("uuid") UUID uuid) {
    AnnotationListImportStatus annotationListImportTaskStatus = taskStatusMap.get(uuid)//
            .orElseThrow(NotFoundException::new);
    return ok(annotationListImportTaskStatus);
  }


  private void parseResources(JsonParser jParser, JsonGenerator jGenerator, String context) throws IOException {
    jGenerator.writeFieldName("resources");
    jParser.nextToken(); // "["
    jGenerator.writeStartArray();
    // parse each resource
    ObjectMapper mapper = new ObjectMapper();
    boolean first = true;
    while (jParser.nextToken() != JsonToken.END_ARRAY) {
      if (!first) {
        jGenerator.writeRaw(",");
      }
      ObjectNode annotationNode = mapper.readTree(jParser);
      String created = Instant.now().toString();
      annotationNode.set("http://purl.org/dc/terms/created", new TextNode(created));
      annotationNode.set("@context", new TextNode(context));
      ObjectNode storedAnnotation = webAnnotationService.validateAndStore(annotationNode);
      jGenerator.writeRaw(new ObjectMapper().writeValueAsString(storedAnnotation));
      first = false;
    }
    jGenerator.writeEndArray();
  }

  private Map<String, Object> dummySequence() {
    Map<String, Object> dummy = baseMap();
    return dummy;
  }

  @Override
  String getType() {
    return "sc:AnnotationList";
  }

}
