package nl.knaw.huygens.alexandria.config;

/*
 * #%L
 * alexandria-main
 * =======
 * Copyright (C) 2015 - 2017 Huygens ING (KNAW)
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
import java.util.Map;

import com.google.common.collect.ImmutableMap;

import nl.knaw.huygens.alexandria.config.AlexandriaConfiguration;

public class MockConfiguration implements AlexandriaConfiguration {

  @Override
  public URI getBaseURI() {
    return URI.create("http://alexandria.eg/");
  }

  @Override
  public String getStorageDirectory() {
    return "/tmp/neo4j-alexandria-mock";
  }

  @Override
  public Map<String, String> getAuthKeyIndex() {
    return ImmutableMap.of("123456", "testuser");
  }

  @Override
  public String getAdminKey() {
    return "whatever";
  }

  @Override
  public Boolean asynchronousEndpointsAllowed() {
    return true;
  }

}
