package nl.knaw.huygens.alexandria.helpers.commands;

import nl.knaw.huygens.alexandria.helpers.FixtureEvaluator;
import nl.knaw.huygens.alexandria.helpers.RestFixture;
import org.concordion.api.AbstractCommand;
import org.concordion.api.Element;
import org.concordion.api.Evaluator;
import org.concordion.api.Result;
import org.concordion.api.ResultRecorder;
import org.concordion.api.listener.AssertEqualsListener;
import org.concordion.api.listener.AssertFailureEvent;
import org.concordion.api.listener.AssertSuccessEvent;
import org.concordion.internal.util.Announcer;

public class HuygensCommand extends AbstractCommand {
  private final Announcer<AssertEqualsListener> listeners = Announcer.to(AssertEqualsListener.class);

  protected void addListener(AssertEqualsListener listener) {
    listeners.addListener(listener);
  }

  protected RestFixture getFixture(Evaluator evaluator) {
    return ((FixtureEvaluator) evaluator).getFixture();
  }

  protected void succeed(ResultRecorder resultRecorder, Element element) {
    resultRecorder.record(Result.SUCCESS);
    announce().successReported(new AssertSuccessEvent(element));
  }

  protected void fail(ResultRecorder resultRecorder, Element element, String actual, String expected) {
    resultRecorder.record(Result.FAILURE);
    announce().failureReported(new AssertFailureEvent(element, expected, actual));
  }

  private AssertEqualsListener announce() {
    return listeners.announce();
  }
}
