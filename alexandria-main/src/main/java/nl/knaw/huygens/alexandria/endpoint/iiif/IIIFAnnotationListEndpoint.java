package nl.knaw.huygens.alexandria.endpoint.iiif;

import static nl.knaw.huygens.alexandria.api.w3c.WebAnnotationConstants.JSONLD_MEDIATYPE;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.glassfish.jersey.server.ChunkedOutput;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.jsonldjava.utils.JsonUtils;
import com.google.common.collect.Maps;

import nl.knaw.huygens.Log;
import nl.knaw.huygens.alexandria.api.model.iiif.IIIFAnnotationList;
import nl.knaw.huygens.alexandria.config.AlexandriaConfiguration;
import nl.knaw.huygens.alexandria.endpoint.webannotation.WebAnnotation;
import nl.knaw.huygens.alexandria.endpoint.webannotation.WebAnnotationService;
import nl.knaw.huygens.alexandria.service.AlexandriaService;

public class IIIFAnnotationListEndpoint extends AbstractIIIFEndpoint {

  private String name;
  private String identifier;
  private WebAnnotationService webAnnotationService;

  public IIIFAnnotationListEndpoint(String identifier, String name, AlexandriaService service, AlexandriaConfiguration config, URI id) {
    super(id);
    this.identifier = identifier;
    this.name = name;
    this.webAnnotationService = new WebAnnotationService(service, config);
  }

  @GET
  public Response get() {
    return notImplemented(dummySequence());
  }

  @POST
  @Consumes(JSONLD_MEDIATYPE)
  public Response postAnnotationList(IIIFAnnotationList annotationList) {
    Map<String, Object> otherProperties = annotationList.getOtherProperties();
    String context = (String) otherProperties.get("@context");
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
  @Path("streaming")
  @Consumes(JSONLD_MEDIATYPE)
  public Response postAnnotationListStreaming(InputStream inputStream) throws JsonParseException, JsonMappingException, IOException {
    StreamingOutput outStream = os -> {
      JsonFactory jsonFactory = new JsonFactory();
      JsonParser jParser = jsonFactory.createParser(inputStream);
      JsonGenerator jGenerator = jsonFactory.createGenerator(os);
      String context;
      jGenerator.writeStartObject();
      while (jParser.nextToken() != JsonToken.END_OBJECT) {
        String fieldname = jParser.getCurrentName();
        Log.info("fieldname={}", fieldname);
        if ("@context".equals(fieldname)) {
          jParser.nextToken();
          context = jParser.getText();
          jGenerator.writeFieldName("@context");
          jGenerator.writeString(context);

        } else if ("resources".equals(fieldname)) {
          parseResources(jParser, jGenerator);

        } else if (fieldname != null) {
          jGenerator.writeFieldName(fieldname);
          jParser.nextToken();
          jGenerator.writeString(jParser.getValueAsString());
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
  public ChunkedOutput<String> postAnnotationListChunked(InputStream inputStream) throws JsonParseException, JsonMappingException, IOException {
    final ChunkedOutput<String> output = new ChunkedOutput<>(String.class);

    AnnotationListHandler alh = new AnnotationListHandler(inputStream);

    new Thread() {
      @Override
      public void run() {
        try {
          String chunk;

          while ((chunk = alh.getNextString()) != null) {
            output.write(chunk);
          }
        } catch (IOException e) {
          // IOException thrown when writing the
          // chunks of response: should be handled
          e.printStackTrace();
        } finally {
          try {
            output.close();
          } catch (IOException e) {
            e.printStackTrace();
          }
        }
      }
    }.start();

    // the output will be probably returned even before
    // a first chunk is written by the new thread
    return output;
  }

  private void parseResources(JsonParser jParser, JsonGenerator jGenerator) throws IOException {
    jGenerator.writeFieldName("resources");
    jParser.nextToken(); // "["
    jGenerator.writeStartArray();
    // parse each resource
    ObjectMapper mapper = new ObjectMapper();
    boolean first = true;
    while (jParser.nextToken() != JsonToken.END_ARRAY) {
      jGenerator.flush();
      if (!first) {
        jGenerator.writeRaw(",");
      }
      ObjectNode resourceNode = mapper.readTree(jParser);
      // Log.info("resourceNode={}", resourceNode.get("resource").get(1).get("chars"));
      jGenerator.writeRaw(resourceNode.toString());
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
