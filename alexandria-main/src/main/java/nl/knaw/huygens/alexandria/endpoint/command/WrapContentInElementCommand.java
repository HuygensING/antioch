package nl.knaw.huygens.alexandria.endpoint.command;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;

import nl.knaw.huygens.Log;
import nl.knaw.huygens.alexandria.api.model.CommandResponse;
import nl.knaw.huygens.alexandria.api.model.Commands;
import nl.knaw.huygens.alexandria.service.AlexandriaService;
import nl.knaw.huygens.alexandria.text.TextUtil;
import nl.knaw.huygens.alexandria.textgraph.TextAnnotation;

public class WrapContentInElementCommand extends TextAnnotationCommand {
  private static class Parameters {
    List<UUID> resourceIds;
    List<String> xmlIds;
    TextAnnotation contentWrapper;
  }

  private static class Context {
    private final AlexandriaService service;
    private final TextAnnotation contentWrapper;
    private final List<String> xmlIds;

    public Context(AlexandriaService service, Parameters parameters) {
      this.service = service;
      this.contentWrapper = parameters.contentWrapper;
      this.xmlIds = parameters.xmlIds;
    }

    public void wrapContent(TextAnnotation parentTextAnnotation) {
      contentWrapper.setDepth(parentTextAnnotation.getDepth() + 1);
      service.wrapContentInChildTextAnnotation(parentTextAnnotation, contentWrapper);
    }

    public boolean hasRelevantXmlId(TextAnnotation textAnnotation) {
      Log.info("textAnnotation={}", textAnnotation);
      String xmlId = textAnnotation.getAttributes().get(TextUtil.XML_ID);
      return xmlIds.contains(xmlId);
    }

  }

  private CommandResponse commandResponse = new CommandResponse();
  private AlexandriaService service;

  @Inject
  public WrapContentInElementCommand(AlexandriaService service) {
    this.service = service;
  }

  @Override
  public CommandResponse runWith(Map<String, Object> parameterMap) {
    Parameters parameters = validateParameters(parameterMap);
    if (commandResponse.parametersAreValid()) {
      for (UUID resourceId : parameters.resourceIds) {
        service.runInTransaction(() -> {
          Context context = new Context(service, parameters);
          service.getTextAnnotationStream(resourceId)//
              .filter(context::hasRelevantXmlId)//
              .forEach(context::wrapContent);
        });
      }
    }
    return commandResponse;
  }

  @SuppressWarnings("unchecked")
  private Parameters validateParameters(Map<String, Object> parameterMap) {
    Parameters parameters = new Parameters();
    parameters.resourceIds = validateResourceIds(parameterMap, commandResponse, service);
    boolean valid = (commandResponse.getErrorLines().isEmpty());

    try {
      parameters.xmlIds = (List<String>) parameterMap.get("xmlIds");
    } catch (ClassCastException e) {
      commandResponse.addErrorLine("Parameter 'xmlIds' should be a list of Strings.");
      valid = false;
    }

    try {
      Map<String, Object> elementMap = (Map) parameterMap.get("element");
      String name = (String) elementMap.get("name");
      Map<String, String> attributes = (Map<String, String>) elementMap.get("attributes");
      TextAnnotation newTextAnnotation = new TextAnnotation(name, attributes, 0);
      parameters.contentWrapper = newTextAnnotation;

    } catch (ClassCastException e) {
      commandResponse.addErrorLine("Parameter 'element' should be a single element with name and attributes.");
      valid = false;
    }

    if (valid) {
      commandResponse.setParametersAreValid(true);
    }
    return parameters;
  }

  @Override
  public String getName() {
    return Commands.WRAP_CONTENT_IN_ELEMENT;
  }

}
