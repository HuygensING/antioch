package nl.knaw.huygens.alexandria.endpoint.command;

import static java.util.stream.Collectors.toList;
import static nl.knaw.huygens.alexandria.text.TextUtil.XML_ID;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import javax.inject.Inject;

import com.google.common.collect.Lists;

import nl.knaw.huygens.alexandria.service.AlexandriaService;
import nl.knaw.huygens.alexandria.textgraph.TextAnnotation;

public class AddUniqueIdCommand implements AlexandriaCommand {
  static final String COMMAND_NAME = "add-unique-id";

  private static class Parameters {
    List<UUID> resourceIds;
    List<String> elementNames;
  }

  private static class Context {
    private static final String XMLID_MARKER = "-";
    private static final Map<String, AtomicLong> counters = new HashMap<>();
    private List<String> existingIds = Lists.newArrayList();
    private AlexandriaService service;

    public Context(AlexandriaService service) {
      this.service = service;
    }

    public void setXmlId(TextAnnotation textAnnotation) {
      String name = textAnnotation.getName();
      String id;
      counters.putIfAbsent(name, new AtomicLong(0));
      do {
        id = name + XMLID_MARKER + counters.get(name).incrementAndGet();
      } while (existingIds.contains(id));
      textAnnotation.getAttributes().put(XML_ID, id);
      service.updateTextAnnotation(textAnnotation);
    }
  }

  private CommandResponse commandResponse = new CommandResponse();
  private AlexandriaService service;

  @Inject
  public AddUniqueIdCommand(AlexandriaService service) {
    this.service = service;
  }

  @Override
  public CommandResponse runWith(Map<String, Object> parameterMap) {
    Parameters parameters = validateParameters(parameterMap);
    if (commandResponse.paremetersAreValid()) {
      for (UUID resourceId : parameters.resourceIds) {
        service.runInTransaction(() -> {
          Context context = new Context(service);
          context.existingIds = service.getTextAnnotationStream(resourceId)//
              .filter(ta -> textAnnotationHasRelevantName(parameters, ta))//
              .filter(this::hasXmlId)//
              .map(this::getXmlId)//
              .collect(toList());
          service.getTextAnnotationStream(resourceId)//
              .filter(ta -> textAnnotationHasRelevantName(parameters, ta))//
              .filter(ta -> !hasXmlId(ta))//
              .forEach(context::setXmlId);
        });
      }
    }
    return commandResponse;
  }

  private String getXmlId(TextAnnotation textAnnotation) {
    return textAnnotation.getAttributes().get(XML_ID);
  }

  private boolean hasXmlId(TextAnnotation textAnnotation) {
    return textAnnotation.getAttributes().containsKey(XML_ID);
  }

  private boolean textAnnotationHasRelevantName(Parameters parameters, TextAnnotation textAnnotation) {
    return parameters.elementNames.contains(textAnnotation.getName());
  }

  private Parameters validateParameters(Map<String, Object> parameterMap) {
    boolean valid = true;
    Parameters parameters = new Parameters();
    try {
      parameters.resourceIds = ((List<String>) parameterMap.get("resourceIds"))//
          .stream()//
          .map(UUID::fromString)//
          .collect(toList());
    } catch (ClassCastException e) {
      commandResponse.addErrorLine("Parameter 'resourceIds' should be list of UUIDs.");
      valid = false;
    }
    try {
      parameters.elementNames = (List<String>) parameterMap.get("elements");
    } catch (ClassCastException e) {
      commandResponse.addErrorLine("Parameter 'elements' should be list of Strings.");
      valid = false;
    }
    if (valid) {
      commandResponse.setParametersAreValid();
    }
    return parameters;
  }

  @Override
  public String getName() {
    return COMMAND_NAME;
  }

}
