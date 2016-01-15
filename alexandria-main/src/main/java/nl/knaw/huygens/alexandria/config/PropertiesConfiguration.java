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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.MissingResourceException;
import java.util.Optional;
import java.util.PropertyResourceBundle;

import nl.knaw.huygens.Log;

public class PropertiesConfiguration {
  private PropertyResourceBundle propertyResourceBundle;

  public PropertiesConfiguration(String propertiesFile, boolean isResource) {
    try {
      InputStream inputStream = isResource ? //
          Thread.currentThread().getContextClassLoader().getResourceAsStream(propertiesFile)//
          : new FileInputStream(new File(propertiesFile));
      propertyResourceBundle = new PropertyResourceBundle(inputStream);
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
