package nl.knaw.huygens.alexandria.endpoint.admin;

import java.io.File;
import java.io.FileInputStream;

/*
 * #%L
 * alexandria-main
 * =======
 * Copyright (C) 2015 Huygens ING (KNAW)
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

import java.io.IOException;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.constraints.NotNull;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import nl.knaw.huygens.Log;
import nl.knaw.huygens.alexandria.config.AlexandriaConfiguration;
import nl.knaw.huygens.alexandria.endpoint.JSONEndpoint;
import nl.knaw.huygens.alexandria.service.AlexandriaService;

@Singleton
@Path("admin")
@Api("admin")
public class AdminEndpoint extends JSONEndpoint {
  private static PropertyResourceBundle propertyResourceBundle;
  private final AlexandriaService service;
  private String adminKey;
  private static String storageDirectory;

  @Inject
  public AdminEndpoint(AlexandriaConfiguration config, AlexandriaService service) {
    this.service = service;
    storageDirectory = config.getStorageDirectory();
    adminKey = getProperty("adminkey");
  }

  private static synchronized String getProperty(String key) {
    if (propertyResourceBundle == null) {
      try {
        String path = storageDirectory + "/alexandria.properties";
        File propertyFile = new File(path);
        if (propertyFile.exists() && propertyFile.canRead()) {
          FileInputStream fileInputStream = new FileInputStream(propertyFile);
          propertyResourceBundle = new PropertyResourceBundle(fileInputStream);
        } else {
          throw new InternalServerErrorException("can't read " + path);
        }
      } catch (IOException e) {
        e.printStackTrace();
        throw new RuntimeException(e);
      }
    }

    try {
      return propertyResourceBundle.getString(key);
    } catch (MissingResourceException e) {
      Log.warn("Missing expected resource: [{}] -- winging it", key);
      return "missing";
    } catch (ClassCastException e) {
      Log.warn("Property value for key [{}] cannot be transformed to String -- winging it", key);
      return "malformed";
    }
  }

  @PUT
  @Consumes(MediaType.APPLICATION_JSON)
  @ApiOperation("do admin tasks")
  public Response doAdminJob(@NotNull AdminJob job) {
    verifyAdminKey(job);
    Object entity = "nothing";
    switch (job.command) {
    case "dump":
      String destination = storageDirectory + "/" + job.parameters.get("filename");
      service.exportDb(job.parameters.get("format"), destination);
      entity = "graph dumped to " + destination;
      break;

    case "read":
      String source = storageDirectory + "/" + job.parameters.get("filename");
      service.importDb(job.parameters.get("format"), source);
      entity = "graph read from " + source;
      break;

    default:
      throw new BadRequestException("command" + job.command + " not recognized");
    }
    return Response.ok(entity).build();
  }

  private void verifyAdminKey(AdminJob job) {
    if (!adminKey.equals(job.key)) {
      throw new ForbiddenException("invalid key");
    }
  }

  static class AdminJob {
    public String key;
    public String command;
    public Map<String, String> parameters;
  }

}
