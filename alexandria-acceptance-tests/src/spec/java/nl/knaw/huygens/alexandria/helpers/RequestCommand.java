package nl.knaw.huygens.alexandria.helpers;

import org.concordion.api.CommandCall;
import org.concordion.api.CommandCallList;
import org.concordion.api.Evaluator;
import org.concordion.api.ResultRecorder;

public class RequestCommand extends HuygensCommand {
  @Override
  public void execute(CommandCall commandCall, Evaluator evaluator, ResultRecorder resultRecorder) {
    final RestFixture fixture = getFixture(evaluator);
    final CommandCallList childCommands = commandCall.getChildren();

    fixture.clear();
    childCommands.setUp(evaluator, resultRecorder);
    childCommands.execute(evaluator, resultRecorder);
    fixture.execute();
    childCommands.verify(evaluator, resultRecorder);
  }
}
