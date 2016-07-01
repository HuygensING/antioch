package nl.knaw.huygens.alexandria.about;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.concordion.integration.junit4.ConcordionRunner;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;

import nl.knaw.huygens.Log;
import nl.knaw.huygens.alexandria.concordion.AlexandriaAcceptanceTest;

/*
 * #%L
 * alexandria-acceptance-tests
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

import nl.knaw.huygens.alexandria.endpoint.about.AboutEndpoint;
import nl.knaw.huygens.alexandria.endpoint.homepage.HomePageEndpoint;

@RunWith(ConcordionRunner.class)
public class AboutFixture extends AlexandriaAcceptanceTest {
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
