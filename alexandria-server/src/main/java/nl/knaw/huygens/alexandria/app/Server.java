package nl.knaw.huygens.alexandria.app;

/*
 * #%L
 * alexandria-server
 * =======
 * Copyright (C) 2015 - 2016 Huygens ING (KNAW)
 * =======
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.Arrays;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import com.google.inject.AbstractModule;
import com.squarespace.jersey2.guice.BootstrapUtils;

import nl.knaw.huygens.Log;
import nl.knaw.huygens.alexandria.config.AlexandriaConfiguration;
import nl.knaw.huygens.alexandria.jersey.AlexandriaApplication;
import nl.knaw.huygens.alexandria.service.AlexandriaService;
import nl.knaw.huygens.alexandria.service.AlexandriaServletModule;
import nl.knaw.huygens.alexandria.text.FileSystemTextService;
import nl.knaw.huygens.alexandria.text.TextService;
import nl.knaw.huygens.alexandria.util.Scheduler;

public class Server {
  private static final long ONE_HOUR = Duration.ofHours(1).toMillis();
  private AlexandriaConfiguration config = new TestConfiguration();

  public static void main(String[] args) throws IOException {
    new Server().run();
  }

  private void run() throws IOException {
    URI uri = getBaseURI();
    final HttpServer httpServer = startServer(uri);
    Log.info("Jersey app started with WADL available at {}/application.wadl\n", uri);
    Scheduler.scheduleExpiredTentativesRemoval();

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
    return config.getBaseURI();
  }

  protected HttpServer startServer(URI uri) throws IOException {
    ServiceLocator locator = createServiceLocator();
    // init service
    AlexandriaService service = locator.getService(AlexandriaService.class);
    Log.info("AlexandriaService {} initialized", service);
    ResourceConfig config = new AlexandriaApplication();
    Log.info("Starting grizzly at {} ...", uri);
    return GrizzlyHttpServerFactory.createHttpServer(uri, config, locator);
  }

  private ServiceLocator createServiceLocator() {
    ServiceLocator locator = BootstrapUtils.newServiceLocator();
    AbstractModule configModule = new AbstractModule() {
      @Override
      protected void configure() {
        bind(AlexandriaConfiguration.class).toInstance(config);
        bind(TextService.class).toInstance(new FileSystemTextService(config.getStorageDirectory() + "/texts"));
      }
    };
    BootstrapUtils.newInjector(locator, Arrays.asList(new AlexandriaServletModule(), configModule));
    BootstrapUtils.install(locator);
    return locator;
  }

}
