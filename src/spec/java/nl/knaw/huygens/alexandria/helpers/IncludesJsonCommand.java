package nl.knaw.huygens.alexandria.helpers;

import java.io.IOException;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.concordion.api.AbstractCommand;
import org.concordion.api.CommandCall;
import org.concordion.api.Element;
import org.concordion.api.Evaluator;
import org.concordion.api.Result;
import org.concordion.api.ResultRecorder;
import org.concordion.api.listener.AssertEqualsListener;
import org.concordion.api.listener.AssertFailureEvent;
import org.concordion.api.listener.AssertSuccessEvent;
import org.concordion.internal.util.Announcer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IncludesJsonCommand extends AbstractCommand {
  private static final Logger LOG = LoggerFactory.getLogger(IncludesJsonCommand.class);

  private Announcer<AssertEqualsListener> listeners = Announcer.to(AssertEqualsListener.class);

  @Override
  public void verify(CommandCall commandCall, Evaluator evaluator, ResultRecorder resultRecorder) {
    final Element element = commandCall.getElement();
    final String expected = element.getText();
//    LOG.trace("IncludesJsonCommand: expected=[{}]", expected);

    final String actual = (String) evaluator.evaluate(commandCall.getExpression());
//    LOG.trace("IncludesJsonCommand: actual=[{}]", actual);

    if (includesJson(actual, expected)) {
      resultRecorder.record(Result.SUCCESS);
      announceSuccess(element);
    } else {
      resultRecorder.record(Result.FAILURE);
      announceFailure(element, expected, actual);
    }
  }

  public void addAssertEqualsListener(AssertEqualsListener listener) {
    listeners.addListener(listener);
  }

  public void removeAssertEqualsListener(AssertEqualsListener listener) {
    listeners.removeListener(listener);
  }

  private void announceSuccess(Element element) {
    listeners.announce().successReported(new AssertSuccessEvent(element));
  }

  private void announceFailure(Element element, String expected, Object actual) {
    listeners.announce().failureReported(new AssertFailureEvent(element, expected, actual));
  }

  private boolean includesJson(String actual, String expected) {
    return includesJson(asJson(actual), asJson(expected));
  }

  private boolean includesJson(JsonNode actual, JsonNode expected) {
    LOG.trace("includesJson.actual  =[{}]", actual);
    LOG.trace("includesJson.expected=[{}]", expected);
    if (expected.isArray()) {
      return includesJsonArray(actual, expected);
    }

    if (expected.isObject()) {
      return includesJsonObject(actual, expected);
    }

    if (expected.isTextual()) {
      // TODO: rather than if-else store these in a mapping from "{format}" to a JsonChecker (to be written) per format
      if ("{date.beforeNow}".equals(expected.asText())) {
        LOG.trace("Parsing [{}] as Instant", actual.asText());
        try {
          final Instant when = Instant.parse(actual.asText());
          return when.isBefore(Instant.now());
        } catch (DateTimeParseException e) {
          LOG.trace("DateTimeParseException: [{}]", e.getMessage());
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

}
