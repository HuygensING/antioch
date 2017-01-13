package nl.knaw.huygens.alexandria.endpoint.iiif;

import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Scanner;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import nl.knaw.huygens.Log;

public class AnnotationListHandler {

  static final PipedInputStream pipedInputStream = new PipedInputStream(2048);
  Scanner scanner = new Scanner(pipedInputStream, "UTF-8");

  public AnnotationListHandler(InputStream inputStream) {
    try {
      JsonFactory jsonFactory = new JsonFactory();
      JsonParser jParser = jsonFactory.createParser(inputStream);
      PipedOutputStream os = new PipedOutputStream(pipedInputStream);

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
          // jParser.skipChildren();

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
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public String getNextString() {
    return scanner.hasNext() ? scanner.next() : null;
  }

  private void parseResources(JsonParser jParser, JsonGenerator jGenerator) throws IOException {
    jGenerator.writeFieldName("resources");
    jParser.nextToken(); // "["
    jGenerator.writeStartArray();
    // parse each resource
    ObjectMapper mapper = new ObjectMapper();
    boolean first = true;
    while (jParser.nextToken() != JsonToken.END_ARRAY) {
      // jGenerator.flush();
      if (!first) {
        jGenerator.writeRaw(",");
      }
      ObjectNode resourceNode = mapper.readTree(jParser);
      Log.info("resourceNode={}", resourceNode.get("resource").get(1).get("chars"));
      jGenerator.writeRaw(resourceNode.toString());
      first = false;
    }
    Log.info("done");
    jGenerator.writeEndArray();
  }

}
