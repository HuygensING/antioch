package nl.knaw.huygens.alexandria;

/*
 * #%L
 * alexandria-main
 * =======
 * Copyright (C) 2015 Huygens ING (KNAW)
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

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.Arrays;
import java.util.List;

import com.google.common.base.MoreObjects;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Scopes;
import com.google.inject.servlet.ServletModule;
import com.squarespace.jersey2.guice.BootstrapUtils;
import nl.knaw.huygens.Log;
import nl.knaw.huygens.alexandria.config.AlexandriaConfiguration;
import nl.knaw.huygens.alexandria.endpoint.EndpointPathResolver;
import nl.knaw.huygens.alexandria.endpoint.annotation.AnnotationEntityBuilder;
import nl.knaw.huygens.alexandria.endpoint.resource.ResourceEntityBuilder;
import nl.knaw.huygens.alexandria.service.AlexandriaService;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;

public abstract class EndpointTest extends JerseyTest {
  private static final AlexandriaConfiguration CONFIG = testConfiguration();
  static TestApplication application;

  protected static void setupWithModule(Module baseModule) {
    Log.debug("Setting up Jersey");

    application = new TestApplication();
    Log.trace("+- application=[{}]", application);

    Log.debug("Bootstrapping Jersey2-Guice bridge");
    ServiceLocator locator = BootstrapUtils.newServiceLocator();
    Log.trace("+- locator=[{}]", locator);

    final List<Module> modules = Arrays.asList(new ServletModule(), baseModule);
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
        return MoreObjects.toStringHelper(this).add("baseURI", getBaseURI()).toString();
      }
    };
  }

  public static class TestModule extends AbstractModule {
    private AlexandriaService serviceInstance;

    public TestModule(AlexandriaService serviceInstance) {
      this.serviceInstance = serviceInstance;
    }

    @Override
    protected void configure() {
      Log.trace("setting up Guice bindings");
      bind(AlexandriaService.class).toInstance(serviceInstance);
      bind(AlexandriaConfiguration.class).toInstance(CONFIG);
      bind(AnnotationEntityBuilder.class).in(Scopes.SINGLETON);
      bind(EndpointPathResolver.class).in(Scopes.SINGLETON);
      bind(ResourceEntityBuilder.class).in(Scopes.SINGLETON);
    }

  }

}
