package nl.knaw.huygens.antioch.config;

/*
 * #%L
 * antioch-main
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

import static java.util.stream.Collectors.toMap;

import java.util.Map;

public class AntiochPropertiesConfiguration {
  private static final String PROPERTIES_FILE = "antioch.properties";
  private static final String PROPERTY_ADMINKEY = "adminkey";
  private static final String PROPERTY_AUTHKEY_PREFIX = "authkey.";
  private final Map<String, String> authKeyIndex;
  private final String adminKey;

  public AntiochPropertiesConfiguration(String storageDirectory) {
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
