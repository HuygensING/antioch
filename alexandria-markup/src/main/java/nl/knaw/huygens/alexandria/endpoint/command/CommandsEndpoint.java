package nl.knaw.huygens.alexandria.endpoint.command;

/*
 * #%L
 * alexandria-main
 * =======
 * Copyright (C) 2015 - 2017 Huygens ING (KNAW)
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
import nl.knaw.huygens.alexandria.api.model.CommandStatus;
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
        .orElseThrow(NotFoundException::new);
    return ok(commandStatus);
  }

}
