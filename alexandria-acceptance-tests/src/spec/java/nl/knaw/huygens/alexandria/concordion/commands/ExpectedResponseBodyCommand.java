package nl.knaw.huygens.alexandria.concordion.commands;

import nl.knaw.huygens.alexandria.concordion.HuygensCommand;
import org.concordion.api.CommandCall;
import org.concordion.api.Element;
import org.concordion.api.Evaluator;
import org.concordion.api.ResultRecorder;
import org.concordion.internal.listener.AssertResultRenderer;

@HuygensCommand(name = "status")
public class ExpectedResponseBodyCommand extends nl.knaw.huygens.alexandria.concordion.commands.HuygensCommand {

  public ExpectedResponseBodyCommand() {
    addListener(new AssertResultRenderer());
  }

  @Override
  public void verify(CommandCall commandCall, Evaluator evaluator, ResultRecorder resultRecorder) {
    final Element element = commandCall.getElement();

    final String expectedStatus = element.getText();
    final String actualStatus = getFixture(evaluator).status();

    if (expectedStatus.equals(actualStatus)) {
      succeed(resultRecorder, element);
    }
    else {
      fail(resultRecorder, element, actualStatus, expectedStatus);
    }
  }
}
