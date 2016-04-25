package nl.knaw.huygens.alexandria.endpoint.command;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;

import nl.knaw.huygens.alexandria.service.AlexandriaService;
import nl.knaw.huygens.alexandria.textgraph.TextAnnotation;

public class WrapContentInElementCommand extends TextAnnotationCommand {
  static final String COMMAND_NAME = "wrap-content-in-element";

  private static class Parameters {
    List<UUID> resourceIds;
    List<String> xmlIds;
    TextAnnotation contentWrapper;
  }

  private static class Context {
    private AlexandriaService service;
    private TextAnnotation contentWrapper;

    public Context(AlexandriaService service, TextAnnotation contentWrapper) {
      this.service = service;
      this.contentWrapper = contentWrapper;
    }

    public void wrapContent(TextAnnotation textAnnotation) {
      int newDepth = textAnnotation.getDepth() + 1;
      contentWrapper.setDepth(newDepth);
      service.insertTextAnnotationAfter(textAnnotation, contentWrapper);
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
    if (commandResponse.paremetersAreValid()) {
      for (UUID resourceId : parameters.resourceIds) {
        service.runInTransaction(() -> {
          Context context = new Context(service, parameters.contentWrapper);
          service.getTextAnnotationStream(resourceId)//
              .filter(parameters.xmlIds::contains)//
              .forEach(context::wrapContent);
        });
      }
    }
    return commandResponse;
  }

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


    } catch (ClassCastException e) {
      commandResponse.addErrorLine("Parameter 'element' should be a single element with name and attributes.");
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
