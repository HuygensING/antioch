package nl.knaw.huygens.alexandria.app;

/*
 * #%L
 * alexandria-server
 * =======
 * Copyright (C) 2015 - 2017 Huygens ING (KNAW)
 * =======
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.AbstractModule;
import com.squarespace.jersey2.guice.BootstrapUtils;

import nl.knaw.huygens.alexandria.config.AlexandriaConfiguration;
import nl.knaw.huygens.alexandria.config.PropertiesConfiguration;
import nl.knaw.huygens.alexandria.jersey.AlexandriaApplication;
import nl.knaw.huygens.alexandria.service.AlexandriaService;
import nl.knaw.huygens.alexandria.service.AlexandriaServletModule;
import nl.knaw.huygens.alexandria.util.Scheduler;

public class Server {
  private static Logger LOG = LoggerFactory.getLogger(Server.class);
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
    LOG.info("Jersey app started with WADL available at {}/application.wadl\n", uri);
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
    LOG.info("AlexandriaService {} initialized", service);
    ResourceConfig config = new AlexandriaApplication();
    LOG.info("Starting grizzly at {} ...", uri);
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
