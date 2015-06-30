package nl.knaw.huygens.alexandria.concordion.commands;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import nl.knaw.huygens.alexandria.concordion.HuygensCommand;
import org.concordion.api.CommandCall;
import org.concordion.api.Element;
import org.concordion.api.Evaluator;
import org.concordion.api.ResultRecorder;
import org.concordion.internal.listener.AssertResultRenderer;

@HuygensCommand(name = "jsonBody", htmlTag = "pre")
public class JsonBodyCommand extends nl.knaw.huygens.alexandria.concordion.commands.HuygensCommand {
  public JsonBodyCommand() {
    addListener(new AssertResultRenderer());
  }

  @Override
  public void setUp(CommandCall commandCall, Evaluator evaluator, ResultRecorder resultRecorder) {
    final Element element = commandCall.getElement();
    element.addStyleClass("json");

    String body = pretty(asJson(element.getText()));
    element.moveChildrenTo(new Element("tmp"));
    element.appendText(body);

    getFixture(evaluator).body(body);
  }

  private JsonNode asJson(String json) {
    try {
      return new ObjectMapper().readTree(json);
    } catch (IOException e) {
      throw new RuntimeException("Failed to parse json: " + json, e);
    }
  }

  private String pretty(JsonNode node) {
    try {
      return new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT).writeValueAsString(node);
    } catch (JsonProcessingException e) {
      throw new RuntimeException("Failed to process json: " + node.asText(), e);
    }
  }
}
