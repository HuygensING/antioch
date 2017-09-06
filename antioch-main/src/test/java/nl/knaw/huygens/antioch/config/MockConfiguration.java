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

import java.net.URI;
import java.util.Map;

import com.google.common.collect.ImmutableMap;

import nl.knaw.huygens.antioch.config.AntiochConfiguration;

public class MockConfiguration implements AntiochConfiguration {

  @Override
  public URI getBaseURI() {
    return URI.create("http://antioch.eg/");
  }

  @Override
  public String getStorageDirectory() {
    return "/tmp/neo4j-antioch-mock";
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