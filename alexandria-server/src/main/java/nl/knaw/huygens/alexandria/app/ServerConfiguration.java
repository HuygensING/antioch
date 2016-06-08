package nl.knaw.huygens.alexandria.app;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/*
 * #%L
 * alexandria-server
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

import java.net.URI;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import nl.knaw.huygens.Log;
import nl.knaw.huygens.alexandria.config.AbstractAlexandriaConfigurationUsingAlexandriaProperties;

public class ServerConfiguration extends AbstractAlexandriaConfigurationUsingAlexandriaProperties {
  private static final File ALEXANDRIA_ROOT = new File(System.getProperty("user.home"), ".alexandria");
  private static final File ALEXANDRIA_PROPERTIES_FILE = new File(ALEXANDRIA_ROOT, "alexandria.properties");

  private static final String PROPERTY_LOG_LEVEL = "logLevel";
  private static final String PROPERTY_ADMIN_KEY = "adminKey";
  private static final String PROPERTY_STORAGE_DIRECTORY = "storageDirectory";
  private static final String PROPERTY_BASE_URI = "baseURI";

  private static final String DEFAULT_BASE_URI = "http://localhost:2015/";
  private static final String DEFAULT_STORAGE_DIRECTORY = new File(ALEXANDRIA_ROOT, "data").getAbsolutePath();
  private static final String DEFAULT_ADMIN_KEY = "admin";

  private String baseURI = DEFAULT_BASE_URI;
  private String storageDirectory = DEFAULT_STORAGE_DIRECTORY;
  private String adminKey = DEFAULT_ADMIN_KEY;
  private Level logLevel = Level.WARN;

  public ServerConfiguration() {
    if (ALEXANDRIA_PROPERTIES_FILE.exists()) {
      initFromFile();
    } else {
      initFromCommandLine();
    }
    setLogLevel();
  }

  @Override
  public URI getBaseURI() {
    return URI.create(baseURI);
  }

  @Override
  public String getStorageDirectory() {
    return storageDirectory;
  }

  @Override
  public Map<String, String> getAuthKeyIndex() {
    return Collections.singletonMap(DEFAULT_ADMIN_KEY, DEFAULT_ADMIN_KEY);
  }

  @Override
  public String getAdminKey() {
    return adminKey;
  }

  @Override
  public Boolean asynchronousEndpointsAllowed() {
    return true;
  }

  private void initFromFile() {
    Properties properties = new Properties();
    try (InputStream in = new FileInputStream(ALEXANDRIA_PROPERTIES_FILE)) {
      System.out.println("Reading properties from " + ALEXANDRIA_PROPERTIES_FILE);
      properties.load(in);
      baseURI = properties.getProperty(PROPERTY_BASE_URI);
      storageDirectory = properties.getProperty(PROPERTY_STORAGE_DIRECTORY);
      adminKey = properties.getProperty(PROPERTY_ADMIN_KEY);
      logLevel = Level.toLevel(properties.getProperty(PROPERTY_LOG_LEVEL));
    } catch (IOException e) {
      e.printStackTrace();
      Log.error("Error reading properties from {}: {}", ALEXANDRIA_PROPERTIES_FILE.getAbsolutePath(), e.getMessage());
    }

  }

  private void initFromCommandLine() {
    try (Scanner keyboard = new Scanner(System.in)) {
      // System.out.println("base uri: (default: " + DEFAULT_BASE_URI + "):");
      // baseURI = StringUtils.defaultIfBlank(keyboard.nextLine(), DEFAULT_BASE_URI);

      Properties properties = new Properties();
      if (!ALEXANDRIA_ROOT.exists()) {
        if (!ALEXANDRIA_ROOT.mkdir()) {
          throw new RuntimeException("Fatal error: couldn't create directory " + ALEXANDRIA_ROOT.getAbsolutePath());
        }
      }
      try (OutputStream out = new FileOutputStream(ALEXANDRIA_PROPERTIES_FILE)) {
        properties.setProperty(PROPERTY_BASE_URI, baseURI);
        properties.setProperty(PROPERTY_STORAGE_DIRECTORY, storageDirectory);
        properties.setProperty(PROPERTY_ADMIN_KEY, adminKey);
        properties.setProperty(PROPERTY_LOG_LEVEL, logLevel.toString());
        properties.store(out, "Alexandria settings");
      } catch (IOException e) {
        e.printStackTrace();
        Log.error("Error writing properties to {}: {}", ALEXANDRIA_PROPERTIES_FILE.getAbsolutePath(), e.getMessage());
      }
    }
  }

  private void setLogLevel() {
    Logger rootLogger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
    rootLogger.setLevel(logLevel);
  }

}
