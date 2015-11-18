package nl.knaw.huygens.alexandria.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;

import javax.inject.Inject;
import javax.ws.rs.InternalServerErrorException;

import nl.knaw.huygens.Log;

public class PropertiesConfiguration {
  private PropertyResourceBundle propertyResourceBundle;
  private AlexandriaConfiguration config;

  @Inject
  public PropertiesConfiguration(AlexandriaConfiguration config) {
    this.config = config;
  }

  public String getProperty(String key) {
    initBundle();
    try {
      return propertyResourceBundle.getString(key);
    } catch (MissingResourceException e) {
      Log.warn("Missing expected resource: [{}] -- winging it", key);
      return "missing";
    } catch (ClassCastException e) {
      Log.warn("Property value for key [{}] cannot be transformed to String -- winging it", key);
      return "malformed";
    }
  }

  public List<String> getKeys() {
    initBundle();
    return Collections.list(propertyResourceBundle.getKeys());
  }

  private void initBundle() {
    if (propertyResourceBundle == null) {
      try {
        String path = config.getStorageDirectory() + "/alexandria.properties";
        File propertyFile = new File(path);
        if (propertyFile.exists() && propertyFile.canRead()) {
          FileInputStream fileInputStream = new FileInputStream(propertyFile);
          propertyResourceBundle = new PropertyResourceBundle(fileInputStream);
        } else {
          throw new InternalServerErrorException("can't read " + path);
        }
      } catch (IOException e) {
        e.printStackTrace();
        throw new RuntimeException(e);
      }
    }
  }

}
