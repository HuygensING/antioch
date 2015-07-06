package nl.knaw.huygens.concordion.commands;

import nl.knaw.huygens.concordion.HuygensCommand;
import org.concordion.api.CommandCall;
import org.concordion.api.Element;
import org.concordion.api.Evaluator;
import org.concordion.api.ResultRecorder;
import org.concordion.api.listener.AssertEqualsListener;
import org.concordion.internal.listener.AssertResultRenderer;
import org.concordion.internal.util.Announcer;

@HuygensCommand(name = "setHeader")
public class SetHeaderCommand extends AbstractHuygensCommand {

  private Announcer<AssertEqualsListener> listeners = Announcer.to(AssertEqualsListener.class);

  public SetHeaderCommand() {
    listeners.addListener(new AssertResultRenderer());
  }

  @Override
  public void setUp(CommandCall commandCall, Evaluator evaluator, ResultRecorder resultRecorder) {
    Element element = commandCall.getElement();
    element.addStyleClass("set-header");

    String name = element.getAttributeValue("name");
    final String value = element.getText();
    getFixture(evaluator).setHeader(name, value);
  }

}