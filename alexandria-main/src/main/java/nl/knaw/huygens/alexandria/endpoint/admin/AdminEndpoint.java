package nl.knaw.huygens.alexandria.endpoint.admin;

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
  private final String adminKey;
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
