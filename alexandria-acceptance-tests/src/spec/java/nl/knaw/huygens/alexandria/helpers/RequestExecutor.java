package nl.knaw.huygens.alexandria.helpers;

import org.concordion.api.Evaluator;

public class RequestExecutor {
  public static final String EXECUTOR_VARIABLE_NAME = "#executor";

  public static RequestExecutor fromEvaluator(Evaluator evaluator) {
    RequestExecutor executor = (RequestExecutor) evaluator.getVariable(EXECUTOR_VARIABLE_NAME);

    if (executor == null) {
      executor = newExecutor(evaluator);
    }

    return executor;
  }

  private static RequestExecutor newExecutor(Evaluator evaluator) {
    final RequestExecutor executor = new RequestExecutor();

    evaluator.setVariable(EXECUTOR_VARIABLE_NAME, executor);

    return executor;
  }
}
