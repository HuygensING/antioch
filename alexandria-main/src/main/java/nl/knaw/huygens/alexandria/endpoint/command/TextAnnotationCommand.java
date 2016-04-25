package nl.knaw.huygens.alexandria.endpoint.command;

import static java.util.stream.Collectors.toList;
import static nl.knaw.huygens.alexandria.text.TextUtil.XML_ID;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import nl.knaw.huygens.alexandria.model.AlexandriaResource;
import nl.knaw.huygens.alexandria.service.AlexandriaService;
import nl.knaw.huygens.alexandria.textgraph.TextAnnotation;

public abstract class TextAnnotationCommand implements AlexandriaCommand {

  List<UUID> validateResourceIds(Map<String, Object> parameterMap, CommandResponse commandResponse, AlexandriaService service) {
    List<UUID> resourceIds = new ArrayList<>();
    try {
      resourceIds = ((List<String>) parameterMap.get("resourceIds"))//
          .stream()//
          .map(UUID::fromString)//
          .collect(toList());
    } catch (ClassCastException | IllegalArgumentException e) {
      commandResponse.addErrorLine("Parameter 'resourceIds' should be list of UUIDs referring to existing resources that have a text.");
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

  String getXmlId(TextAnnotation textAnnotation) {
    return textAnnotation.getAttributes().get(XML_ID);
  }

  boolean hasXmlId(TextAnnotation textAnnotation) {
    return textAnnotation.getAttributes().containsKey(XML_ID);
  }

}
