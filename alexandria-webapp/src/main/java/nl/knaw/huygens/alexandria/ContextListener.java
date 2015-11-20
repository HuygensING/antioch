package nl.knaw.huygens.alexandria;

/*
 * #%L
 * alexandria-webapp
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
import nl.knaw.huygens.alexandria.config.AbstractAlexandriaConfigurationUsingAlexandriaProperties;
import nl.knaw.huygens.alexandria.config.AlexandriaConfiguration;
import nl.knaw.huygens.alexandria.config.PropertiesConfiguration;
import nl.knaw.huygens.alexandria.service.AlexandriaServletModule;
import nl.knaw.huygens.alexandria.util.Scheduler;

@WebListener
public class ContextListener extends JerseyGuiceServletContextListener {
  public static final String CONFIG_FILE = "config.properties";
  public static final String BASE_URI_PROP = "baseURI";
  public static final String STORAGE_DIRECTORY_PROP = "storageDirectory";

  @Override
  public void contextInitialized(ServletContextEvent servletContextEvent) {
    Scheduler.scheduleExpiredTentativesRemoval();
  }

  @Override
  public void contextDestroyed(ServletContextEvent sce) {
    Log.info("contextDestroyed called");
    getInjector()//
        .getInstance(new AlexandriaServletModule().getTinkerPopServiceClass())//
        .destroy();
    super.contextDestroyed(sce);
    Log.info("contextDestroyed done");
  }

  @Override
  protected List<? extends Module> modules() {
    return ImmutableList.of(new AlexandriaServletModule(), configurationModule());
  }

  private Module configurationModule() {
    return new AbstractModule() {
      @Override
      protected void configure() {
        bind(AlexandriaConfiguration.class).toInstance(cache(propertyBackedConfiguration()));
      }
    };
  }

  private AlexandriaConfiguration cache(AlexandriaConfiguration delegate) {
    return new CachedConfiguration(delegate);
  }

  private AlexandriaConfiguration propertyBackedConfiguration() {
    return new AbstractAlexandriaConfigurationUsingAlexandriaProperties() {
      private PropertiesConfiguration properties;

      @Override
      public URI getBaseURI() {
        return UriBuilder.fromUri(getProperties().getProperty(BASE_URI_PROP).get()).build();
      }

      @Override
      public String getStorageDirectory() {
        return getProperties().getProperty(STORAGE_DIRECTORY_PROP).get();
      }

      private PropertiesConfiguration getProperties() {
        if (properties == null) {
          properties = new PropertiesConfiguration(CONFIG_FILE, true);
        }
        return properties;
      }
    };
  }

  private static class CachedConfiguration extends AbstractAlexandriaConfigurationUsingAlexandriaProperties {
    private final AlexandriaConfiguration delegate;

    private URI cachedBaseURI;
    private String cachedStorageDirectory;

    public CachedConfiguration(AlexandriaConfiguration delegate) {
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
  // private static final PropertyResourceBundle propertyResourceBundle = readResourceBundle();
  //
  // private static PropertyResourceBundle readResourceBundle() {
  // try {
  // return new PropertyResourceBundle(readConfigFile());
  // } catch (IOException e) {
  // final String message = String.format("Failed to read configuration from: [%s]", CONFIG_FILE);
  // Log.error(message);
  // throw new RuntimeException(message, e);
  // }
  // }
  //
  // private static InputStream readConfigFile() {
  // final InputStream resources = Thread.currentThread().getContextClassLoader().getResourceAsStream(CONFIG_FILE);
  //
  // if (resources == null) {
  // final String message = String.format("Missing configuration file: [%s]", CONFIG_FILE);
  // Log.error(message);
  // throw new RuntimeException(message);
  // }
  //
  // return resources;
  // }
}
