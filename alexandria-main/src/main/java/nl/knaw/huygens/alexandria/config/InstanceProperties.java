package nl.knaw.huygens.alexandria.config;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

public class InstanceProperties {
  PropertiesConfiguration properties;

  @Inject
  public InstanceProperties(AlexandriaConfiguration config) {
    properties = new PropertiesConfiguration(config.getStorageDirectory() + "/alexandria.properties", false);
  }

  public Optional<String> getProperty(String key) {
    return properties.getProperty(key);
  }

  public String getProperty(String key, String defaultValue) {
    return properties.getProperty(key, defaultValue);
  }

  public List<String> getKeys() {
    return properties.getKeys();
  }

}
