package nl.knaw.huygens.concordion.commands;

import nl.knaw.huygens.concordion.HuygensCommand;
import nl.knaw.huygens.concordion.RestFixture;
import org.concordion.api.CommandCall;
import org.concordion.api.Element;
import org.concordion.api.Evaluator;
import org.concordion.api.ResultRecorder;
import org.concordion.internal.listener.AssertResultRenderer;

@HuygensCommand(name = "success", htmlTag = "span")
public class ExpectedSuccessCommand extends AbstractHuygensCommand {

  public ExpectedSuccessCommand() {
    addListener(new AssertResultRenderer());
  }

  @Override
  public void verify(CommandCall commandCall, Evaluator evaluator, ResultRecorder resultRecorder) {
    final Element element = commandCall.getElement();
    final String expectedStatus = element.getText();

    final RestFixture fixture = getFixture(evaluator);
    final String actualStatus = fixture.status();

    if (fixture.wasSuccessful()) {
      succeed(resultRecorder, element);
    } else {
      fail(resultRecorder, element, actualStatus, expectedStatus);
    }
  }
}
