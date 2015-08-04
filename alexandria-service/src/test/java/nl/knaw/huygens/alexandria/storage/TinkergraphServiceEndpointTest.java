package nl.knaw.huygens.alexandria.storage;

import org.junit.BeforeClass;

import com.google.inject.Module;

import nl.knaw.huygens.alexandria.EndpointTest;
import nl.knaw.huygens.alexandria.service.AlexandriaService;
import nl.knaw.huygens.alexandria.service.TinkerGraphService;

public class TinkergraphServiceEndpointTest extends EndpointTest {
  static final AlexandriaService service = new TinkerGraphService();

  @BeforeClass
  public static void setup() {
    Module baseModule = new TestModule(service);
    setupWithModule(baseModule);
  }

  public AlexandriaService getService() {
    return service;
  }

}
