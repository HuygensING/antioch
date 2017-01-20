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
import com.fasterxml.jackson.databind.node.TextNode;
import com.github.jsonldjava.utils.JsonUtils;
import com.google.common.collect.Maps;

import nl.knaw.huygens.alexandria.api.model.iiif.IIIFAnnotationList;
import nl.knaw.huygens.alexandria.config.AlexandriaConfiguration;
import nl.knaw.huygens.alexandria.endpoint.webannotation.WebAnnotation;
import nl.knaw.huygens.alexandria.endpoint.webannotation.WebAnnotationService;
import nl.knaw.huygens.alexandria.exception.BadRequestException;
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
  // @Path("streaming")
  @Consumes(JSONLD_MEDIATYPE)
  public Response postAnnotationListStreaming(InputStream inputStream) throws JsonParseException, JsonMappingException, IOException {
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
  public ChunkedOutput<String> postAnnotationListChunked(InputStream inputStream) throws JsonParseException, JsonMappingException, IOException {
    final ChunkedOutput<String> output = new ChunkedOutput<>(String.class);
    AnnotationListHandler alh = new AnnotationListHandler(inputStream, webAnnotationService);
    new Thread() {
      @Override
      public void run() {
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
      }
    }.start();
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
