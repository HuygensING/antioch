package nl.knaw.huygens.alexandria;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/*
 * #%L
 * alexandria-main
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

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.UriBuilder;

import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableMap;
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
    return Entity.json(fixQuotes(string));
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

      @Override
      public Map<String, String> getAuthKeyIndex() {
        return ImmutableMap.of("123456", "testuser");
      }

      @Override
      public String getAdminKey() {
        return "whatever";
      }

      @Override
      public Boolean asynchronousEndpointsAllowed() {
        return true;
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

  protected String fixQuotes(String stringWithSingleQuotes) {
    return stringWithSingleQuotes.replace("'", "\"");
  }

}
