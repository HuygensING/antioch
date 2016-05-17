package nl.knaw.huygens.alexandria.app;

import java.io.File;
import java.net.NetworkInterface;
import java.net.SocketException;

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

import javax.ws.rs.core.UriBuilder;

import nl.knaw.huygens.alexandria.config.AbstractAlexandriaConfigurationUsingAlexandriaProperties;

public class ServerConfiguration extends AbstractAlexandriaConfigurationUsingAlexandriaProperties {

  @Override
  public URI getBaseURI() {
    return URI.create("http://localhost:2015/");
  }

  @Override
  public String getStorageDirectory() {
    return new File(System.getProperty("user.dir", "."), "alexandria-data").getAbsolutePath();
  }

  @Override
  public Map<String, String> getAuthKeyIndex() {
    return Collections.singletonMap("admin", "admin");
  }

  @Override
  public String getAdminKey() {
    return "admin";
  }

  @Override
  public Boolean asynchronousEndpointsAllowed() {
    return true;
  }

}
