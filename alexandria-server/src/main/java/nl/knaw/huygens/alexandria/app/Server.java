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
import nl.knaw.huygens.alexandria.config.PropertiesConfiguration;
import nl.knaw.huygens.alexandria.jersey.AlexandriaApplication;
import nl.knaw.huygens.alexandria.service.AlexandriaService;
import nl.knaw.huygens.alexandria.service.AlexandriaServletModule;
import nl.knaw.huygens.alexandria.util.Scheduler;

public class Server {
  private static final long ONE_HOUR = Duration.ofHours(1).toMillis();
  private AlexandriaConfiguration config = new ServerConfiguration();
  private static final String PROPERTIES_FILE = "about.properties";

  public static void main(String[] args) throws IOException {
    System.out.println("-----------------------------");
    System.out.println("Starting Alexandria server...");
    PropertiesConfiguration propertiesConfiguration = new PropertiesConfiguration(PROPERTIES_FILE, true);
    System.out.println("version  : " + propertiesConfiguration.getProperty("version").get());
    System.out.println("buildDate: " + propertiesConfiguration.getProperty("buildDate").get());
    System.out.println("commitId : " + propertiesConfiguration.getProperty("commitId").get());
    System.out.println("-----------------------------");
    new Server().run();
  }

  private void run() throws IOException {
    URI uri = getBaseURI();
    final HttpServer httpServer = startServer(uri);
    Log.info("Jersey app started with WADL available at {}/application.wadl\n", uri);
    System.out.println("-----------------------------");
    System.out.println("Alexandria server started at " + uri);
    System.out.println("press Ctrl-c to stop");
    System.out.println("-----------------------------");

    Runtime.getRuntime().addShutdownHook(new Thread(() -> shutdown(httpServer)));

    while (true) {
      try {
        Thread.sleep(ONE_HOUR);
      } catch (InterruptedException e) {
        System.out.println("-----------------------------");
        System.out.println("Stopping Alexandria server...");
        System.out.println("-----------------------------");
        shutdown(httpServer);
        System.out.println("bye!");
      }
    }
  }

  private void shutdown(final HttpServer httpServer) {
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
    Scheduler scheduler = locator.getService(Scheduler.class);
    scheduler.scheduleExpiredTentativesRemoval();
    URI uri0000 = URI.create("http://0.0.0.0:" + uri.getPort());
    return GrizzlyHttpServerFactory.createHttpServer(uri0000, config, locator);
  }

  private ServiceLocator createServiceLocator() {
    ServiceLocator locator = BootstrapUtils.newServiceLocator();
    AbstractModule configModule = new AbstractModule() {
      @Override
      protected void configure() {
        bind(AlexandriaConfiguration.class).toInstance(config);
      }
    };
    BootstrapUtils.newInjector(locator, Arrays.asList(new AlexandriaServletModule(), configModule));
    BootstrapUtils.install(locator);
    return locator;
  }

}
