package nl.knaw.huygens.alexandria.storage;

import org.junit.BeforeClass;

import com.google.inject.Module;

import nl.knaw.huygens.alexandria.EndpointTest;
import nl.knaw.huygens.alexandria.service.AlexandriaService;
import nl.knaw.huygens.alexandria.service.TinkerpopAlexandriaService;

public class TinkergraphServiceEndpointTest extends EndpointTest {
  static Storage storage = new TinkerGraphStorage();
  static final AlexandriaService tinkerGraphService = new TinkerpopAlexandriaService().withStorage(storage);

  @BeforeClass
  public static void setup() {
    Module baseModule = new TestModule(tinkerGraphService, storage);
    setupWithModule(baseModule);
  }

  public Storage getStorage() {
    return storage;
  }

}
