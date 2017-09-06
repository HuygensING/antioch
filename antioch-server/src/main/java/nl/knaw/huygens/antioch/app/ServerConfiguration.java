package nl.knaw.huygens.antioch.app;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import nl.knaw.huygens.antioch.config.AbstractAntiochConfigurationUsingAntiochProperties;

/*
 * #%L
 * antioch-server
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

public class ServerConfiguration extends AbstractAntiochConfigurationUsingAntiochProperties {
  private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(ServerConfiguration.class);
  private static final File ANTIOCH_ROOT = new File(System.getProperty("user.home"), ".alexandria");
  private static final File ANTIOCH_PROPERTIES_FILE = new File(ANTIOCH_ROOT, "alexandria.properties");

  private static final String PROPERTY_LOG_LEVEL = "logLevel";
  private static final String PROPERTY_ADMIN_KEY = "adminKey";
  private static final String PROPERTY_STORAGE_DIRECTORY = "storageDirectory";
  private static final String PROPERTY_BASE_URI = "baseURI";

  private static final String DEFAULT_BASE_URI = "http://localhost:2015/";
  private static final String DEFAULT_STORAGE_DIRECTORY = new File(ANTIOCH_ROOT, "data").getAbsolutePath();
  private static final String DEFAULT_ADMIN_KEY = "admin";

  private String baseURI = DEFAULT_BASE_URI;
  private String storageDirectory = DEFAULT_STORAGE_DIRECTORY;
  private String adminKey = DEFAULT_ADMIN_KEY;
  private Level logLevel = Level.WARN;

  public ServerConfiguration() {
    if (ANTIOCH_PROPERTIES_FILE.exists()) {
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
    try (InputStream in = new FileInputStream(ANTIOCH_PROPERTIES_FILE)) {
      System.out.println("Reading properties from " + ANTIOCH_PROPERTIES_FILE);
      properties.load(in);
      baseURI = properties.getProperty(PROPERTY_BASE_URI);
      storageDirectory = properties.getProperty(PROPERTY_STORAGE_DIRECTORY);
      adminKey = properties.getProperty(PROPERTY_ADMIN_KEY);
      logLevel = Level.toLevel(properties.getProperty(PROPERTY_LOG_LEVEL));
    } catch (IOException e) {
      e.printStackTrace();
      LOG.error("Error reading properties from {}: {}", ANTIOCH_PROPERTIES_FILE.getAbsolutePath(), e.getMessage());
    }

  }

  private void initFromCommandLine() {
    try (Scanner keyboard = new Scanner(System.in)) {
      // System.out.println("base uri: (default: " + DEFAULT_BASE_URI + "):");
      // baseURI = StringUtils.defaultIfBlank(keyboard.nextLine(), DEFAULT_BASE_URI);

      Properties properties = new Properties();
      if (!ANTIOCH_ROOT.exists()) {
        if (!ANTIOCH_ROOT.mkdir()) {
          throw new RuntimeException("Fatal error: couldn't create directory " + ANTIOCH_ROOT.getAbsolutePath());
        }
      }
      try (OutputStream out = new FileOutputStream(ANTIOCH_PROPERTIES_FILE)) {
        properties.setProperty(PROPERTY_BASE_URI, baseURI);
        properties.setProperty(PROPERTY_STORAGE_DIRECTORY, storageDirectory);
        properties.setProperty(PROPERTY_ADMIN_KEY, adminKey);
        properties.setProperty(PROPERTY_LOG_LEVEL, logLevel.toString());
        properties.store(out, "Antioch settings");
      } catch (IOException e) {
        e.printStackTrace();
        LOG.error("Error writing properties to {}: {}", ANTIOCH_PROPERTIES_FILE.getAbsolutePath(), e.getMessage());
      }
    }
  }

  private void setLogLevel() {
    Logger rootLogger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
    rootLogger.setLevel(logLevel);
  }

}
