package nl.knaw.huygens.alexandria.client;

import java.net.URI;
import java.util.Arrays;
import java.util.Map;

import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import com.google.common.collect.ImmutableMap;
import com.google.inject.AbstractModule;
import com.squarespace.jersey2.guice.BootstrapUtils;

import nl.knaw.huygens.Log;
import nl.knaw.huygens.alexandria.config.AlexandriaConfiguration;
import nl.knaw.huygens.alexandria.jersey.AlexandriaApplication;
import nl.knaw.huygens.alexandria.service.AlexandriaService;
import nl.knaw.huygens.alexandria.service.AlexandriaServletModule;
import nl.knaw.huygens.alexandria.service.TinkerPopService;
import nl.knaw.huygens.alexandria.storage.Storage;
import nl.knaw.huygens.alexandria.text.InMemoryTextService;
import nl.knaw.huygens.alexandria.text.TextService;

public abstract class AlexandriaClientTest {
  protected static final String AUTHKEY = "AUTHKEY";
  // URI testURI = URI.create("http://test.alexandria.huygens.knaw.nl/");
  static URI testURI = URI.create("http://localhost:2016/");
  AlexandriaClient client = new AlexandriaClient(testURI);
  private static HttpServer server;
  private static AlexandriaConfiguration config = new AlexandriaConfiguration() {
    @Override
    public String getStorageDirectory() {
      return System.getProperty("java.io.tmpdir");
    }

    @Override
    public URI getBaseURI() {
      return testURI;
    }

    @Override
    public Map<String, String> getAuthKeyIndex() {
      return ImmutableMap.of("AUTHKEY", "testuser");
    }

    @Override
    public String getAdminKey() {
      return "adminkey";
    }
  };

  @BeforeClass
  public static void startTestServer() {
    ServiceLocator locator = createServiceLocator();
    // init service
    AlexandriaService service = locator.getService(AlexandriaService.class);
    Log.info("AlexandriaService {} initialized", service);
    ResourceConfig config = new AlexandriaApplication();
    ((TinkerPopService) service).setStorage(new Storage(TinkerGraph.open()));
    Log.info("Starting grizzly at {} ...", testURI);
    server = GrizzlyHttpServerFactory.createHttpServer(testURI, config, locator);
    service.destroy();
  }

  @AfterClass
  public static void stopTestServer() {
    server.shutdown();
  }

  private static ServiceLocator createServiceLocator() {
    ServiceLocator locator = BootstrapUtils.newServiceLocator();
    AbstractModule configModule = new AbstractModule() {
      @Override
      protected void configure() {
        bind(AlexandriaConfiguration.class).toInstance(config);
        bind(TextService.class).toInstance(new InMemoryTextService());
      }
    };
    BootstrapUtils.newInjector(locator, Arrays.asList(new AlexandriaServletModule(), configModule));
    BootstrapUtils.install(locator);
    return locator;
  }

}
