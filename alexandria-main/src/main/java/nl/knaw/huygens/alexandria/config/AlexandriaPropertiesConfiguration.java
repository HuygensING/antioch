package nl.knaw.huygens.alexandria.config;

/*
 * #%L
 * alexandria-main
 * =======
 * Copyright (C) 2015 - 2016 Huygens ING (KNAW)
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
