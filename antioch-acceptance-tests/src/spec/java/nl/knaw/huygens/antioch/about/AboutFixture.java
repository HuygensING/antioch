package nl.knaw.huygens.antioch.about;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.concordion.integration.junit4.ConcordionRunner;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;

import nl.knaw.huygens.Log;
import nl.knaw.huygens.antioch.concordion.AntiochAcceptanceTest;

/*
 * #%L
 * antioch-acceptance-tests
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

import nl.knaw.huygens.antioch.endpoint.about.AboutEndpoint;
import nl.knaw.huygens.antioch.endpoint.homepage.HomePageEndpoint;

@RunWith(ConcordionRunner.class)
public class AboutFixture extends AntiochAcceptanceTest {
  @BeforeClass
  public static void registerEndpoint() {
    register(AboutEndpoint.class);
    register(HomePageEndpoint.class);
  }

  public String projectVersion() {
    final Properties properties = new Properties();
    try (final InputStream stream = this.getClass().getResourceAsStream("/.properties")) {
      Log.info("stream={}", stream);
      properties.load(stream);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return properties.getProperty("project_version");
  }

}
