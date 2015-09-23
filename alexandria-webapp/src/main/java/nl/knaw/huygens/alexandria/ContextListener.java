package nl.knaw.huygens.alexandria;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;

import javax.servlet.ServletContextEvent;
import javax.servlet.annotation.WebListener;
import javax.ws.rs.core.UriBuilder;

import com.google.common.collect.ImmutableList;
import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.squarespace.jersey2.guice.JerseyGuiceServletContextListener;

import nl.knaw.huygens.Log;
import nl.knaw.huygens.alexandria.config.AlexandriaConfiguration;
import nl.knaw.huygens.alexandria.config.AlexandriaServletModule;
import nl.knaw.huygens.alexandria.util.Scheduler;

@WebListener
public class ContextListener extends JerseyGuiceServletContextListener {
  public static final String CONFIG_FILE = "config.properties";
  public static final String BASE_URI_PROP = "baseURI";
  public static final String STORAGE_DIRECTORY_PROP = "storageDirectory";

  private static final PropertyResourceBundle propertyResourceBundle = readResourceBundle();

  private static PropertyResourceBundle readResourceBundle() {
    try {
      return new PropertyResourceBundle(readConfigFile());
    } catch (IOException e) {
      final String message = String.format("Failed to read configuration from: [%s]", CONFIG_FILE);
      Log.error(message);
      throw new RuntimeException(message, e);
    }
  }

  private static InputStream readConfigFile() {
    final InputStream resources = Thread.currentThread().getContextClassLoader().getResourceAsStream(CONFIG_FILE);

    if (resources == null) {
      final String message = String.format("Missing configuration file: [%s]", CONFIG_FILE);
      Log.error(message);
      throw new RuntimeException(message);
    }

    return resources;
  }

  @Override
  protected List<? extends Module> modules() {
    return ImmutableList.of(new AlexandriaServletModule(), configurationModule());
  }

  @Override
  public void contextInitialized(ServletContextEvent servletContextEvent) {
    Scheduler.scheduleExpiredTentativesRemoval();
  }

  private String getProperty(String key) {
    Log.trace("getProperty: [{}]", key);
    try {
      final String value = propertyResourceBundle.getString(key);
      Log.trace("+- bound to: [{}]", value);
      return value;
    } catch (MissingResourceException e) {
      Log.warn("Missing expected resource: [{}] -- winging it", key);
      return "missing";
    } catch (ClassCastException e) {
      Log.warn("Property value for key [{}] cannot be transformed to String -- winging it", key);
      return "malformed";
    }
  }

  private Module configurationModule() {
    return new AbstractModule() {
      @Override
      protected void configure() {
        bind(AlexandriaConfiguration.class).toInstance(bindConfiguration());
      }
    };
  }

  private AlexandriaConfiguration bindConfiguration() {
    return new AlexandriaConfiguration() {
      private final URI baseURI = UriBuilder.fromUri(getProperty(BASE_URI_PROP)).build();
      private final String storageDirectory = getProperty(STORAGE_DIRECTORY_PROP);

      @Override
      public URI getBaseURI() {
        return baseURI;
      }

      @Override
      public String getStorageDirectory() {
        return storageDirectory;
      }
    };
  }
}