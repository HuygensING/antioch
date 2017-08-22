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

import java.net.URI;
import java.util.Map;

public interface AlexandriaConfiguration {

  /**
   * 
   * @return the URI for the (proxied) instance of this server
   */
  URI getBaseURI();

  /**
   * 
   * @return the directory where database and instance configuration is stored
   */
  String getStorageDirectory();

  /**
   * 
   * @return an index mapping authKeys to userNames
   */
  Map<String, String> getAuthKeyIndex();

  /**
   * 
   * @return the adminKey to use in the AdminEndpoint
   */
  String getAdminKey();

  /**
   *
   * @return whether endpoint may be asynchronous (start a thread)
   */
  Boolean asynchronousEndpointsAllowed();
}
