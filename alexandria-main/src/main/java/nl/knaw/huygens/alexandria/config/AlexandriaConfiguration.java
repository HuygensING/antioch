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
