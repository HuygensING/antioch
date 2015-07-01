package nl.knaw.huygens.alexandria.endpoint;

import static org.mockito.Mockito.mock;

import org.junit.BeforeClass;

import com.google.inject.Module;

import nl.knaw.huygens.alexandria.EndpointTest;
import nl.knaw.huygens.alexandria.service.AlexandriaService;

public class MockedServiceEndpointTest extends EndpointTest {
  protected static final AlexandriaService SERVICE_MOCK = mock(AlexandriaService.class);

  @BeforeClass
  public static void setup() {
    Module baseModule = new TestModule(SERVICE_MOCK);
    setupWithModule(baseModule);
  }

}
