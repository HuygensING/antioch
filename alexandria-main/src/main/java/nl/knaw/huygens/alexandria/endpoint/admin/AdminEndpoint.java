package nl.knaw.huygens.alexandria.endpoint.admin;

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

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.constraints.NotNull;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import nl.knaw.huygens.alexandria.config.AlexandriaConfiguration;
import nl.knaw.huygens.alexandria.endpoint.JSONEndpoint;
import nl.knaw.huygens.alexandria.service.AlexandriaService;

@Singleton
@Path("admin")
@Api("admin")
public class AdminEndpoint extends JSONEndpoint {
  private final AlexandriaService service;
  private String adminKey;
  private static String storageDirectory;

  @Inject
  public AdminEndpoint(AlexandriaConfiguration config, AlexandriaService service) {
    this.service = service;
    storageDirectory = config.getStorageDirectory();
    adminKey = config.getAdminKey();
  }

  @PUT
  @Consumes(MediaType.APPLICATION_JSON)
  @ApiOperation("do admin tasks")
  public Response doAdminJob(@NotNull AdminJob job) {
    verifyAdminKey(job);
    final Object entity;
    switch (job.command) {
    case "export":
      String destination = absoluteFilePath(job);
      service.exportDb(job.parameters.get("format"), destination);
      entity = "graph dumped to " + destination;
      break;

    case "import":
      String source = absoluteFilePath(job);
      service.importDb(job.parameters.get("format"), source);
      entity = "graph read from " + source;
      break;

    default:
      throw new BadRequestException("command " + job.command + " not recognized");
    }
    return ok(entity);
  }

  private String absoluteFilePath(AdminJob job) {
    String fileName = job.parameters.get("filename");
    return fileName.startsWith("/") ? fileName : (storageDirectory + "/" + fileName);
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
