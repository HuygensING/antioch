package nl.knaw.huygens.alexandria.endpoint.command;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import nl.knaw.huygens.alexandria.api.model.CommandResponse;
import nl.knaw.huygens.alexandria.model.AlexandriaResource;
import nl.knaw.huygens.alexandria.service.AlexandriaService;

public abstract class ResourcesCommand implements AlexandriaCommand {

  private static final String RESOURCE_IDS = "resourceIds";

  List<UUID> validateResourceIds(Map<String, Object> parameterMap, CommandResponse commandResponse, AlexandriaService service) {
    List<UUID> resourceIds = new ArrayList<>();

    if (!parameterMap.containsKey(RESOURCE_IDS)) {
      addResourceIdsError(commandResponse);
      return resourceIds;
    }

    try {
      resourceIds = ((List<String>) parameterMap.get(RESOURCE_IDS))//
          .stream()//
          .map(UUID::fromString)//
          .collect(toList());
    } catch (ClassCastException | IllegalArgumentException e) {
      addResourceIdsError(commandResponse);
    }

    for (UUID resourceId : resourceIds) {
      Optional<AlexandriaResource> optionalResource = service.readResource(resourceId);
      if (optionalResource.isPresent()) {
        if (!optionalResource.get().hasText()) {
          commandResponse.addErrorLine("resource '" + resourceId + "' does not have a text.");
        }
      } else {
        commandResponse.addErrorLine("resourceId '" + resourceId + "' does not exist.");
      }
    }
    return resourceIds;
  }

  private CommandResponse addResourceIdsError(CommandResponse commandResponse) {
    return commandResponse.addErrorLine("Parameter '" + RESOURCE_IDS + "' should be a list of UUIDs referring to existing resources that have a text.");
  }

}
