package nl.knaw.huygens.alexandria.helpers;

import java.io.IOException;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.base.Strings;
import nl.knaw.huygens.Log;
import org.concordion.api.CommandCall;
import org.concordion.api.Element;
import org.concordion.api.Evaluator;
import org.concordion.api.ResultRecorder;
import org.concordion.internal.listener.AssertResultRenderer;

public class ExpectedJsonResponseCommand extends HuygensCommand {
  public ExpectedJsonResponseCommand() {
    addListener(new AssertResultRenderer());
  }

  @Override
  public void verify(CommandCall commandCall, Evaluator evaluator, ResultRecorder resultRecorder) {
    final Element element = commandCall.getElement();
    final JsonNode expectedJson = asJson(element.getText());
    final String expected = pretty(expectedJson);
    element.moveChildrenTo(new Element("tmp"));
    element.appendText(expected);

    final String actual = getFixture(evaluator).response();
    if (Strings.isNullOrEmpty(actual)) {
      fail(resultRecorder, element, "(not set)", expected);
    } else {
      final JsonNode actualJson = asJson(actual);
      if (includesJson(actualJson, expectedJson)) {
        succeed(resultRecorder, element);
      } else {
        fail(resultRecorder, element, pretty(actualJson), expected);
      }
    }
  }

  private boolean includesJson(JsonNode actual, JsonNode expected) {
    Log.trace("includesJson.actual  =[{}]", actual);
    Log.trace("includesJson.expected=[{}]", expected);
    if (expected.isArray()) {
      return includesJsonArray(actual, expected);
    }

    if (expected.isObject()) {
      return includesJsonObject(actual, expected);
    }

    if (expected.isTextual()) {
      // TODO: rather than if-else store these in a mapping from "{format}" to a JsonChecker (to be written) per format
      switch (expected.asText()) {
        case "{date.beforeNow}":
          Log.trace("Parsing [{}] as Instant", actual.asText());
          try {
            final Instant when = Instant.parse(actual.asText());
            return when.isBefore(Instant.now());
          } catch (DateTimeParseException e) {
            Log.trace("DateTimeParseException: [{}]", e.getMessage());
            return false;
          }
      }
    }

    return actual.equals(expected);
  }

  private boolean includesJsonArray(JsonNode actual, JsonNode expected) {
    if (!actual.isArray()) {
      return false;
    }

    if (expected.size() == 0) {
      return actual.size() == 0;
    }

    outer:
    for (JsonNode expectedItem : expected) {
      for (JsonNode candidateItem : actual) {
        if (includesJson(candidateItem, expectedItem)) {
          continue outer;
        }
      }
      return false;
    }

    return true;
  }

  private boolean includesJsonObject(JsonNode actual, JsonNode expected) {
    if (!actual.isObject()) {
      return false;
    }

    final Iterator<Entry<String, JsonNode>> expectedFields = expected.fields();
    while (expectedFields.hasNext()) {
      final Map.Entry<String, JsonNode> expectedEntry = expectedFields.next();

      final String expectedProperty = expectedEntry.getKey();
      if (!actual.has(expectedProperty)) {
        return false;
      }

      final JsonNode actualValue = actual.get(expectedProperty);
      final JsonNode expectedValue = expectedEntry.getValue();
      if (!includesJson(actualValue, expectedValue)) {
        return false;
      }
    }

    return true;
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