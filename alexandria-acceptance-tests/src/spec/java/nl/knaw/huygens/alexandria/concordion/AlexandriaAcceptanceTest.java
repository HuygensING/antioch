package nl.knaw.huygens.alexandria.concordion;

import static java.lang.String.format;

import java.net.URI;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.UriBuilder;

import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;
import org.concordion.api.extension.Extension;
import org.junit.BeforeClass;

import com.google.common.base.Objects;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.Scopes;

import nl.knaw.huygens.Log;
import nl.knaw.huygens.alexandria.config.AlexandriaConfiguration;
import nl.knaw.huygens.alexandria.endpoint.EndpointPathResolver;
import nl.knaw.huygens.alexandria.endpoint.LocationBuilder;
import nl.knaw.huygens.alexandria.endpoint.annotation.AnnotationEntityBuilder;
import nl.knaw.huygens.alexandria.endpoint.resource.ResourceEntityBuilder;
import nl.knaw.huygens.alexandria.service.AlexandriaService;
import nl.knaw.huygens.alexandria.service.TinkerGraphService;
import nl.knaw.huygens.alexandria.service.TinkerPopService;
import nl.knaw.huygens.alexandria.storage.Storage;
import nl.knaw.huygens.alexandria.util.UUIDParser;
import nl.knaw.huygens.cat.RestExtension;
import nl.knaw.huygens.cat.RestFixture;

public class AlexandriaAcceptanceTest extends RestFixture {
  private static final AlexandriaConfiguration CONFIG = testConfiguration();

  private static Storage storage = tinkerGraphStorage();

  private static LocationBuilder locationBuilder = new LocationBuilder(testConfiguration(), new EndpointPathResolver());

  private static TinkerPopService service = new TinkerPopService(storage, locationBuilder);

  @Extension
  // @SuppressWarnings("unused")
  public RestExtension extensionFoundViaReflection = new RestExtension().enableCodeMirror().includeBootstrap();

  @BeforeClass
  public static void setupAlexandriaAcceptanceTest() {
    Log.debug("Setting up AlexandriaAcceptanceTest");
    setupRestFixture(alexandriaModule());
    register(JsonConfiguration.class);
  }

  private static Storage tinkerGraphStorage() {
    return new Storage(TinkerGraph.open());
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

  private static Module alexandriaModule() {
    return new AbstractModule() {
      @Override
      protected void configure() {
        Log.trace("setting up Guice bindings");
        bind(TinkerPopService.class).to(TinkerGraphService.class);
        bind(AlexandriaService.class).toInstance(service);
        bind(AlexandriaConfiguration.class).toInstance(CONFIG);
        bind(AnnotationEntityBuilder.class).in(Scopes.SINGLETON);
        bind(EndpointPathResolver.class).in(Scopes.SINGLETON);
        bind(ResourceEntityBuilder.class).in(Scopes.SINGLETON);
      }
    };
  }

  @Override
  public void clear() {
    Log.debug("Clearing {}", getClass().getSimpleName());
    super.clear();
  }

  public String uuid() {
    return Iterables.getLast(Splitter.on('/').split(location().orElse("(not set)")));
  }

  @Override
  protected Application configure() {
    return super.configure(); // maybe move enabling of LOG_TRAFFIC and DUMP_ENTITY from RestFixture here?
  }

  public void clearStorage() {
    Log.debug("Clearing Storage");
    service.setStorage(tinkerGraphStorage());
  }

  protected AlexandriaService service() {
    return service;
  }

  private Optional<UUID> parse(String idStr) {
    return UUIDParser.fromString(idStr).get();
  }

  private Supplier<String> malformedDescription(String idStr) {
    return () -> "malformed UUID: " + idStr;
  }

  private String normalizeHostInfo(String s) {
    return s.replaceAll(hostInfo(), "{host}");
  }

  private String hostInfo() {
    final URI baseURI = getBaseUri();
    return format("%s:%d", baseURI.getHost(), baseURI.getPort());
  }

  private String baseOf(String s) {
    return s.substring(0, s.lastIndexOf('/') + 1);
  }

  private String tailOf(String s) {
    return Iterables.getLast(Splitter.on('/').split(s));
  }
}
