package nl.knaw.huygens.alexandria.config;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.MissingResourceException;
import java.util.Optional;
import java.util.PropertyResourceBundle;

import nl.knaw.huygens.Log;

public class PropertiesConfiguration {
  private PropertyResourceBundle propertyResourceBundle;

  public PropertiesConfiguration(String propertiesFile) {
    try {
      propertyResourceBundle = new PropertyResourceBundle(//
          Thread.currentThread().getContextClassLoader().getResourceAsStream(propertiesFile));
    } catch (IOException e) {
      e.printStackTrace();
      throw new RuntimeException("Couldn't read properties file " + propertiesFile + ": " + e.getMessage());
    }
  }

  public synchronized Optional<String> getProperty(String key) {
    return Optional.ofNullable(getValue(key));
  }

  public synchronized String getProperty(String key, String defaultValue) {
    String value = getValue(key);
    return value != null ? value : defaultValue;
  }

  private String getValue(String key) {
    String value = null;
    try {
      value = propertyResourceBundle.getString(key);
    } catch (MissingResourceException e) {
      Log.warn("Missing expected resource: [{}]", key);
    } catch (ClassCastException e) {
      Log.warn("Property value for key [{}] cannot be transformed to String", key);
    }
    return value;
  }

  public List<String> getKeys() {
    return Collections.list(propertyResourceBundle.getKeys());
  }

}