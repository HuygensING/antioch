package nl.knaw.huygens.alexandria.endpoint.command;

import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import com.google.common.base.Joiner;

import nl.knaw.huygens.alexandria.api.EndpointPaths;
import nl.knaw.huygens.alexandria.api.model.CommandResponse;
import nl.knaw.huygens.alexandria.api.model.ProcessStatusMap;
import nl.knaw.huygens.alexandria.endpoint.JSONEndpoint;
import nl.knaw.huygens.alexandria.endpoint.LocationBuilder;
import nl.knaw.huygens.alexandria.exception.NotFoundException;

@Singleton
@Path(EndpointPaths.COMMANDS)
public class CommandsEndpoint extends JSONEndpoint {

  private AlexandriaCommandProcessor commandProcessor;
  private LocationBuilder locationBuilder;
  private final ProcessStatusMap<CommandStatus> commandStatusMap;

  @Inject
  public CommandsEndpoint(AlexandriaCommandProcessor commandProcessor, //
      LocationBuilder locationBuilder, //
      ProcessStatusMap<CommandStatus> commandStatusMap) {
    this.commandProcessor = commandProcessor;
    this.locationBuilder = locationBuilder;
    this.commandStatusMap = commandStatusMap;
  }

  @POST
  @Path("{commandName}")
  public Response doCommand(@PathParam("commandName") String commandName, Map<String, Object> parameterMap) {
    CommandResponse commandResponse = commandProcessor.process(commandName, parameterMap);
    if (!commandResponse.parametersAreValid()) {
      throw new BadRequestException(Joiner.on(", ").join(commandResponse.getErrorLines()));
    }
    if (commandResponse.isASync()) {
      return Response.accepted()//
          .location(locationBuilder.locationOf(EndpointPaths.COMMANDS, commandName, commandResponse.getStatusId(), "status"))//
          .build();
    }
    return ok(commandResponse);
  }

  @GET
  @Path("{commandName}/{statusId}/status")
  public Response getCommandStatus(@PathParam("commandName") String commandName, @PathParam("statusId") UUID statusId) {
    CommandStatus commandStatus = commandStatusMap.get(statusId)//
        .orElseThrow(() -> new NotFoundException());
    return ok(commandStatus);
  }

}
