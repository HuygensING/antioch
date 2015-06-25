package nl.knaw.huygens.alexandria.app;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.Arrays;

import nl.knaw.huygens.Log;
import nl.knaw.huygens.alexandria.config.AlexandriaServletModule;
import nl.knaw.huygens.alexandria.config.TinkerpopAlexandriaConfiguration;
import nl.knaw.huygens.alexandria.jersey.AlexandriaApplication;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import com.squarespace.jersey2.guice.BootstrapUtils;

public class Server {
  private static final long ONE_HOUR = Duration.ofHours(1).toMillis();

  public static void main(String[] args) throws IOException {
    new Server().run();
  }

  private void run() throws IOException {
    URI uri = getBaseURI();
    final HttpServer httpServer = startServer(uri);
    Log.info("Jersey app started with WADL available at {}/application.wadl\n", uri);

    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      shutdown(httpServer);
    }));

    while (true) {
      try {
        Thread.sleep(ONE_HOUR);
      } catch (InterruptedException e) {
        shutdown(httpServer);
      }
    }
  }

  private void shutdown(final HttpServer httpServer) {
    Log.info("Stopping HTTP server");
    httpServer.shutdown();
  }

  private URI getBaseURI() {
    return new TinkerpopAlexandriaConfiguration().getBaseURI();
  }

  protected HttpServer startServer(URI uri) throws IOException {
    ServiceLocator locator = BootstrapUtils.newServiceLocator();
    BootstrapUtils.newInjector(locator, Arrays.asList(new AlexandriaServletModule()));
    BootstrapUtils.install(locator);
    ResourceConfig config = new AlexandriaApplication();
    Log.info("Starting grizzly at {} ...", uri);
    return GrizzlyHttpServerFactory.createHttpServer(uri, config, locator);
  }

}
