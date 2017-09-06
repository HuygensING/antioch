package nl.knaw.huygens.antioch;

/*
 * #%L
 * antioch-webapp
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

import java.net.URI;
import java.util.List;

import javax.servlet.ServletContextEvent;
import javax.servlet.annotation.WebListener;
import javax.ws.rs.core.UriBuilder;

import com.google.common.collect.ImmutableList;
import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.squarespace.jersey2.guice.JerseyGuiceServletContextListener;

import nl.knaw.huygens.Log;
import nl.knaw.huygens.antioch.config.AbstractAntiochConfigurationUsingAntiochProperties;
import nl.knaw.huygens.antioch.config.AntiochConfiguration;
import nl.knaw.huygens.antioch.config.PropertiesConfiguration;
import nl.knaw.huygens.antioch.service.AntiochServletModule;
import nl.knaw.huygens.antioch.util.Scheduler;

@WebListener
public class ContextListener extends JerseyGuiceServletContextListener {
  private static final String CONFIG_FILE = "config.properties";
  private static final String BASE_URI_PROP = "baseURI";
  private static final String STORAGE_DIRECTORY_PROP = "storageDirectory";

  @Override
  protected List<? extends Module> modules() {
    return ImmutableList.of(new AntiochServletModule(), configurationModule());
  }

  @Override
  public void contextInitialized(ServletContextEvent servletContextEvent) {
    getInjector().getInstance(Scheduler.class).scheduleExpiredTentativesRemoval();
  }

  @Override
  public void contextDestroyed(ServletContextEvent sce) {
    Log.info("contextDestroyed called");
    getInjector()//
        .getInstance(new AntiochServletModule().getTinkerPopServiceClass())//
        .destroy();
    super.contextDestroyed(sce);
    Log.info("contextDestroyed done");
  }

  private Module configurationModule() {
    return new AbstractModule() {
      @Override
      protected void configure() {
        AntiochConfiguration configuration = cache(propertyBackedConfiguration());
        bind(AntiochConfiguration.class).toInstance(configuration);
      }
    };
  }

  private AntiochConfiguration cache(AntiochConfiguration delegate) {
    return new CachedConfiguration(delegate);
  }

  private AntiochConfiguration propertyBackedConfiguration() {
    return new AbstractAntiochConfigurationUsingAntiochProperties() {
      private PropertiesConfiguration properties;

      @Override
      public URI getBaseURI() {
        return UriBuilder.fromUri(getProperties().getProperty(BASE_URI_PROP).get()).build();
      }

      private PropertiesConfiguration getProperties() {
        if (properties == null) {
          properties = new PropertiesConfiguration(CONFIG_FILE, true);
        }
        return properties;
      }

      @Override
      public String getStorageDirectory() {
        return getProperties().getProperty(STORAGE_DIRECTORY_PROP).get();
      }
    };
  }

  private static class CachedConfiguration extends AbstractAntiochConfigurationUsingAntiochProperties {
    private final AntiochConfiguration delegate;

    private URI cachedBaseURI;
    private String cachedStorageDirectory;

    public CachedConfiguration(AntiochConfiguration delegate) {
      this.delegate = delegate;
    }

    @Override
    public URI getBaseURI() {
      if (cachedBaseURI == null) {
        cachedBaseURI = delegate.getBaseURI();
      }

      return cachedBaseURI;
    }

    @Override
    public String getStorageDirectory() {
      if (cachedStorageDirectory == null) {
        cachedStorageDirectory = delegate.getStorageDirectory();
      }

      return cachedStorageDirectory;
    }

  }
}
