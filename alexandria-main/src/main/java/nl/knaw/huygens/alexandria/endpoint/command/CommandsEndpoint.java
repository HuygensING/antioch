package nl.knaw.huygens.alexandria.endpoint.command;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.google.common.base.Joiner;

import nl.knaw.huygens.alexandria.api.EndpointPaths;
import nl.knaw.huygens.alexandria.endpoint.JSONEndpoint;

@Singleton
@Path(EndpointPaths.COMMANDS)
public class CommandsEndpoint extends JSONEndpoint {

  private AlexandriaCommandProcessor commandProcessor;

  @Inject
  public CommandsEndpoint(AlexandriaCommandProcessor commandProcessor) {
    this.commandProcessor = commandProcessor;
  }

  @POST
  @Path("{commandName}")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response doCommand(@PathParam("commandName") String commandName, Map<String, Object> parameterMap) {
    CommandResponse commandResponse = commandProcessor.process(commandName, parameterMap);
    if (!commandResponse.paremetersAreValid) {
      throw new BadRequestException(Joiner.on(", ").join(commandResponse.getErrorLines()));
    }
    return ok(commandResponse);
  }

}
