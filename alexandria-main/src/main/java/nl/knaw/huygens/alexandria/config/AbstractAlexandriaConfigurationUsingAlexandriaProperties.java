package nl.knaw.huygens.alexandria.config;

import java.util.Map;

public abstract class AbstractAlexandriaConfigurationUsingAlexandriaProperties implements AlexandriaConfiguration {
  private AlexandriaPropertiesConfiguration alexandriaPropertiesConfiguration = new AlexandriaPropertiesConfiguration(getStorageDirectory());

  @Override
  public Map<String, String> getAuthKeyIndex() {
    return alexandriaPropertiesConfiguration.getAuthKeyIndex();
  }

  @Override
  public String getAdminKey() {
    return alexandriaPropertiesConfiguration.getAdminKey();
  }

}
