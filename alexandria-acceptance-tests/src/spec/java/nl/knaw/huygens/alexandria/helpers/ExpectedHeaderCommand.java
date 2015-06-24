package nl.knaw.huygens.alexandria.helpers;

import org.concordion.api.CommandCall;
import org.concordion.api.Element;
import org.concordion.api.Evaluator;
import org.concordion.api.ResultRecorder;
import org.concordion.internal.listener.AssertResultRenderer;

public class ExpectedHeaderCommand extends HuygensCommand {

  public ExpectedHeaderCommand() {
    addListener(new AssertResultRenderer());
  }

  @Override
  public void verify(CommandCall commandCall, Evaluator evaluator, ResultRecorder resultRecorder) {
    final Element element = commandCall.getElement();
    element.addStyleClass("header");

    final String expectedHeader = element.getText();
    final String actualHeader = getFixture(evaluator).header(element.getAttributeValue("name")).orElse("(not set)");

    if (actualHeader.equals(expectedHeader)) {
      succeed(resultRecorder, element);
    }
    else {
      fail(resultRecorder, element, actualHeader, expectedHeader);
    }
  }
}
