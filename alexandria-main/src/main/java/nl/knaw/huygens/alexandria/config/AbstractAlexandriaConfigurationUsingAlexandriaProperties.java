package nl.knaw.huygens.alexandria.config;

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

import java.util.Map;

public abstract class AbstractAlexandriaConfigurationUsingAlexandriaProperties implements AlexandriaConfiguration {
  private AlexandriaPropertiesConfiguration alexandriaPropertiesConfiguration;

  @Override
  public Map<String, String> getAuthKeyIndex() {
    return getAlexandriaPropertiesConfiguration().getAuthKeyIndex();
  }

  @Override
  public String getAdminKey() {
    return getAlexandriaPropertiesConfiguration().getAdminKey();
  }

  @Override
  public Boolean asynchronousEndpointsAllowed() {
    return true;
  }

  private AlexandriaPropertiesConfiguration getAlexandriaPropertiesConfiguration() {
    if (alexandriaPropertiesConfiguration == null) {
      alexandriaPropertiesConfiguration = new AlexandriaPropertiesConfiguration(getStorageDirectory());
    }
    return alexandriaPropertiesConfiguration;
  }

}
