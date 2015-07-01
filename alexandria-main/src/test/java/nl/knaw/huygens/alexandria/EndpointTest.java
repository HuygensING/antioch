package nl.knaw.huygens.alexandria.endpoint;

import static org.mockito.Mockito.mock;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.UriBuilder;

import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.junit.BeforeClass;

import com.google.common.base.Objects;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Scopes;
import com.google.inject.servlet.ServletModule;
import com.squarespace.jersey2.guice.BootstrapUtils;

import nl.knaw.huygens.Log;
import nl.knaw.huygens.alexandria.config.AlexandriaConfiguration;
import nl.knaw.huygens.alexandria.endpoint.annotation.AnnotationEntityBuilder;
import nl.knaw.huygens.alexandria.endpoint.resource.ResourceEntityBuilder;
import nl.knaw.huygens.alexandria.service.AlexandriaService;

public class EndpointTest extends JerseyTest {
  protected static final AlexandriaService SERVICE_MOCK = mock(AlexandriaService.class);
  private static final AlexandriaConfiguration CONFIG = testConfiguration();
  static TestApplication application;

  @BeforeClass
  public static void setup() {
    Log.debug("Setting up Jersey");

    application = new TestApplication();
    Log.trace("+- application=[{}]", application);

    Log.debug("Bootstrapping Jersey2-Guice bridge");
    ServiceLocator locator = BootstrapUtils.newServiceLocator();
    Log.trace("+- locator=[{}]", locator);

    final List<Module> modules = Arrays.asList(new ServletModule(), baseModule());
    final Injector injector = BootstrapUtils.newInjector(locator, modules);
    Log.trace("+- injector=[{}]", injector);

    BootstrapUtils.install(locator);
    Log.trace("+- done: locator installed");
  }

  @Override
  protected Application configure() {
    enable(TestProperties.LOG_TRAFFIC);
    enable(TestProperties.DUMP_ENTITY);
    return application;
  }

  protected static void register(Class<?> componentClass) {
    application.register(componentClass);
  }

  protected Entity<String> jsonEntity(String string) {
    return Entity.json(string.replace("'", "\""));
  }

  private static AlexandriaConfiguration testConfiguration() {
    return new AlexandriaConfiguration() {
      @Override
      public URI getBaseURI() {
        return UriBuilder.fromUri("https://localhost/").port(4242).build();
      }

      @Override
      public String getStorageDirectory() {
        return "/tmp";
      }

      @Override
      public String toString() {
        return Objects.toStringHelper(this).add("baseURI", getBaseURI()).toString();
      }
    };
  }

  private static Module baseModule() {
    return new AbstractModule() {
      @Override
      protected void configure() {
        Log.trace("setting up Guice bindings");
        bind(AlexandriaService.class).toInstance(SERVICE_MOCK);
        bind(AlexandriaConfiguration.class).toInstance(CONFIG);
        bind(AnnotationEntityBuilder.class).in(Scopes.SINGLETON);
        bind(EndpointPathResolver.class).in(Scopes.SINGLETON);
        bind(ResourceEntityBuilder.class).in(Scopes.SINGLETON);
      }
    };
  }

}
