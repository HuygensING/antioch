package nl.knaw.huygens.alexandria;

import org.concordion.integration.junit4.ConcordionRunner;
import org.junit.runner.RunWith;

@RunWith(ConcordionRunner.class)
public class HelloWorldFixture {
  public String getGreeting() {
    return "Hello World!";
  }
}
