package nl.knaw.huygens.alexandria.endpoint.iiif;

import java.io.IOException;
import java.io.InputStream;
import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicBoolean;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class AnnotationListHandler {

  private JsonParser jParser;
  private String context;
  private Deque<String> deque = new ConcurrentLinkedDeque<>();
  private AtomicBoolean firstField = new AtomicBoolean(true);

  public AnnotationListHandler(InputStream inputStream) {
    try {
      jParser = new JsonFactory().createParser(inputStream);
      deque.add("{");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public String getNextString() {
    try {
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
            parseResources(jParser);
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
    } catch (IOException e) {
      e.printStackTrace();
    }
    return deque.isEmpty() ? null : deque.pop();
  }

  private void parseResources(JsonParser jParser) throws IOException {
    deque.add("\"resources\":[");
    jParser.nextToken(); // "["
    // parse each resource
    boolean first = true;
    while (jParser.nextToken() != JsonToken.END_ARRAY) {
      if (!first) {
        deque.add(",");
      }
      ObjectNode resourceNode = new ObjectMapper().readTree(jParser);
      deque.add(resourceNode.toString());
      first = false;
    }
    deque.add("]");
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
