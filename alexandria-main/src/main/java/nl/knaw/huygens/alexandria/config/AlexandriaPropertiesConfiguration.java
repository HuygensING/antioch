package nl.knaw.huygens.alexandria.config;

import static java.util.stream.Collectors.toMap;

import java.util.Map;

public class AlexandriaPropertiesConfiguration {
  private static final String PROPERTIES_FILE = "alexandria.properties";
  private static final String PROPERTY_ADMINKEY = "adminkey";
  private static final String PROPERTY_AUTHKEY_PREFIX = "authkey.";
  private Map<String, String> authKeyIndex;
  private String adminKey;

  public AlexandriaPropertiesConfiguration(String storageDirectory) {
    PropertiesConfiguration properties = new PropertiesConfiguration(storageDirectory + "/" + PROPERTIES_FILE, false);
    authKeyIndex = properties.getKeys().stream()//
        .filter(k -> k.startsWith(PROPERTY_AUTHKEY_PREFIX))//
        .map(k -> k.replaceFirst(PROPERTY_AUTHKEY_PREFIX, ""))//
        .collect(toMap(//
            name -> properties.getProperty(PROPERTY_AUTHKEY_PREFIX + name).get(), //
            name -> name));
    adminKey = properties.getProperty(PROPERTY_ADMINKEY).get();
  }

  public Map<String, String> getAuthKeyIndex() {
    return authKeyIndex;
  }

  public String getAdminKey() {
    return adminKey;
  }

}
