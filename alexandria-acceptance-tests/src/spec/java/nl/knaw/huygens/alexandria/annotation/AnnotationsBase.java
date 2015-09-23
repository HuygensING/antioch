package nl.knaw.huygens.alexandria.annotation;

import org.junit.BeforeClass;

import nl.knaw.huygens.alexandria.concordion.AlexandriaAcceptanceTest;
import nl.knaw.huygens.alexandria.endpoint.annotation.AnnotationsEndpoint;
import nl.knaw.huygens.alexandria.endpoint.resource.ResourcesEndpoint;

public class AnnotationsBase extends AlexandriaAcceptanceTest {
  @BeforeClass
  public static void registerEndpoint() {
    register(AnnotationsEndpoint.class);
    register(ResourcesEndpoint.class);
  }
}
