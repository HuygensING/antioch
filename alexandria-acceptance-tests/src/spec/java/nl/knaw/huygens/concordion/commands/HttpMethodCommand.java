package nl.knaw.huygens.concordion.commands;

import org.concordion.api.CommandCall;
import org.concordion.api.Element;
import org.concordion.api.Evaluator;
import org.concordion.api.ResultRecorder;

abstract public class HttpMethodCommand extends AbstractHuygensCommand {
  private final String method;

  public HttpMethodCommand(String method) {
    this.method = method;
  }

  @Override
  public void setUp(CommandCall commandCall, Evaluator evaluator, ResultRecorder resultRecorder) {
    final Element element = commandCall.getElement();
    element.addStyleClass(method.toLowerCase());

    getFixture(evaluator) //
        .method(method) //
        .url(element.getText());
  }
}
