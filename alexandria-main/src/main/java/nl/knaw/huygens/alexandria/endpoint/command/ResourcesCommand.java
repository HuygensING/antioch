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

import static java.util.stream.Collectors.toList;

import java.util.*;

import com.google.common.base.Splitter;

import nl.knaw.huygens.alexandria.api.model.CommandResponse;
import nl.knaw.huygens.alexandria.model.AlexandriaResource;
import nl.knaw.huygens.alexandria.service.AlexandriaService;

public abstract class ResourcesCommand implements AlexandriaCommand {

  private static final String RESOURCE_IDS = "resourceIds";

  List<ResourceViewId> validateResourceViewIds(Map<String, Object> parameterMap, CommandResponse commandResponse, AlexandriaService service) {
    List<ResourceViewId> resourceViewIds = new ArrayList<>();

    if (!parameterMap.containsKey(RESOURCE_IDS)) {
      addResourceIdsError(commandResponse);
      return resourceViewIds;
    }

    try {
      resourceViewIds = ((List<String>) parameterMap.get(RESOURCE_IDS))//
          .stream()//
          .map(this::split)//
          .flatMap(List::stream)//
          .collect(toList());
    } catch (ClassCastException | IllegalArgumentException e) {
      addResourceIdsError(commandResponse);
    }

    for (ResourceViewId resourceViewId : resourceViewIds) {
      UUID resourceId = resourceViewId.getResourceId();
      Optional<AlexandriaResource> optionalResource = service.readResource(resourceId);
      if (optionalResource.isPresent()) {
        if (!optionalResource.get().hasText()) {
          commandResponse.addErrorLine("resource '" + resourceId + "' does not have a text.");
        }

        resourceViewId.getTextViewName().ifPresent(viewId -> {
          boolean viewExists = service.getTextViewsForResource(resourceId)//
              .stream()//
              .anyMatch(tv -> tv.getName().equals(viewId));
          if (!viewExists) {
            commandResponse.addErrorLine("resourceId '" + resourceId + "' does not have a view '" + viewId + "'.");
          }
        });

      } else {
        commandResponse.addErrorLine("resourceId '" + resourceId + "' does not exist.");
      }
    }
    return resourceViewIds;

  }

  List<ResourceViewId> split(String resourceViewIds) {
    if (resourceViewIds.contains(",")) {
      String[] parts = resourceViewIds.split(":");
      UUID uuid = UUID.fromString(parts[0]);
      return Splitter.on(",").splitToList(parts[1]).stream()//
          .map(name -> new ResourceViewId(uuid, name))//
          .collect(toList());
    }

    return Collections.singletonList(ResourceViewId.fromString(resourceViewIds));
  }

  private CommandResponse addResourceIdsError(CommandResponse commandResponse) {
    return commandResponse.addErrorLine("Parameter '" + RESOURCE_IDS
        + "' should be a list of UUIDs, optionally with one or more view ids, (e.g. ['3e8c6332-230c-4fc5-865f-0d51534f4375:view-1,view2']) referring to existing resources that have a text.");
  }

}
