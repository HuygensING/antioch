package nl.knaw.huygens.alexandria.annotation;

import nl.knaw.huygens.alexandria.endpoint.annotation.AnnotationsEndpoint;
import nl.knaw.huygens.alexandria.concordion.AlexandriaAcceptanceTest;
import org.junit.BeforeClass;

public class AnnotationsBase extends AlexandriaAcceptanceTest {
  @BeforeClass
  public static void registerEndpoint() {
    register(AnnotationsEndpoint.class);
  }
}
