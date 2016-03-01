package nl.knaw.huygens.alexandria.client;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.util.UUID;

import org.junit.Ignore;
import org.junit.Test;

import nl.knaw.huygens.Log;
import nl.knaw.huygens.alexandria.api.model.AboutEntity;

@Ignore
public class AlexandriaClientTest {
  private static final String AUTHKEY = System.getProperty("ALEXANDRIA_AUTHKEY_TEST", "YHJZHjpke8JYjm5y");
  URI testURI = URI.create("http://test.alexandria.huygens.knaw.nl/");
  // URI testURI = URI.create("http://localhost:2016/");
  AlexandriaClient client = new AlexandriaClient(testURI);
  // private HttpServer server;
  //
  // @BeforeClass
  // public void startTestServer() {
  // ServiceLocator locator = createServiceLocator();
  // // init service
  // AlexandriaService service = locator.getService(AlexandriaService.class);
  // Log.info("AlexandriaService {} initialized", service);
  // ResourceConfig config = new AlexandriaApplication();
  // Log.info("Starting grizzly at {} ...", testURI);
  // server = GrizzlyHttpServerFactory.createHttpServer(testURI, config, locator);
  // }
  //
  // @AfterClass
  // public void stopTestServer() {
  // server.shutdown();
  // }
  //
  // private ServiceLocator createServiceLocator() {
  // ServiceLocator locator = BootstrapUtils.newServiceLocator();
  // AbstractModule configModule = new AbstractModule() {
  // @Override
  // protected void configure() {
  // bind(AlexandriaConfiguration.class).toInstance(config);
  // bind(TextService.class).toInstance(new FileSystemTextService(config.getStorageDirectory() + "/texts"));
  // }
  // };
  // BootstrapUtils.newInjector(locator, Arrays.asList(new AlexandriaServletModule(), configModule));
  // BootstrapUtils.install(locator);
  // return locator;
  // }

  @Test
  public void testGetAboutReturnsValidAboutEntity() {
    RestResult<AboutEntity> result = client.getAbout();
    assertThat(result.hasFailed()).isFalse();
    AboutEntity about = result.get();
    Log.info("about={}", about);
    assertThat(about).isNotNull();
  }

  @Test
  public void testAddResourceReturnsValidUUID() {
    client.setAuthKey(AUTHKEY);
    ResourcePrototype resource = new ResourcePrototype("corpus");
    RestResult<UUID> result = client.addResource(resource);
    assertThat(result.hasFailed()).isFalse();
    UUID resourceUuid = result.get();
    Log.info("resourceUUID = {}", resourceUuid);
    assertThat(resourceUuid).isNotNull();

    // retrieve the resource
    RestResult<ResourceEntity> result2 = client.getResource(resourceUuid);
    assertThat(result2.hasFailed()).isFalse();
    ResourceEntity resourceEntity = result2.get();
    assertThat(resourceEntity).isNotNull();
  }

  @Test
  public void testAddResourceWithProvenanceReturnsValidUUID() {
    client.setAuthKey(AUTHKEY);
    ResourcePrototype resource = new ResourcePrototype("corpus2").withProvenance(new ProvenancePrototype().setWho("test").setWhy("because test"));
    RestResult<UUID> result = client.addResource(resource);
    assertThat(result.hasFailed()).isFalse();
    UUID resourceUuid = result.get();
    Log.info("resourceUUID = {}", resourceUuid);
    assertThat(resourceUuid).isNotNull();
  }

}
