package nl.knaw.huygens.alexandria.endpoint.admin;

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
import nl.knaw.huygens.alexandria.config.InstanceProperties;
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
  public AdminEndpoint(AlexandriaConfiguration aconfig, InstanceProperties properties, AlexandriaService service) {
    this.service = service;
    storageDirectory = aconfig.getStorageDirectory();
    adminKey = properties.getProperty("adminkey").get();
  }

  @PUT
  @Consumes(MediaType.APPLICATION_JSON)
  @ApiOperation("do admin tasks")
  public Response doAdminJob(@NotNull AdminJob job) {
    verifyAdminKey(job);
    Object entity = "nothing";
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
    return Response.ok(entity).build();
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
