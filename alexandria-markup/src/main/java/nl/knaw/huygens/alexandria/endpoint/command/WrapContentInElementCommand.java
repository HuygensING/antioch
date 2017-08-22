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

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import nl.knaw.huygens.alexandria.api.model.CommandResponse;
import nl.knaw.huygens.alexandria.api.model.Commands;
import nl.knaw.huygens.alexandria.service.MarkupService;
import nl.knaw.huygens.alexandria.text.TextUtil;
import nl.knaw.huygens.alexandria.textgraph.TextAnnotation;

public class WrapContentInElementCommand extends TextAnnotationCommand {
  private static class Parameters {
    List<ResourceViewId> resourceViewIds;
    List<String> xmlIds;
    TextAnnotation contentWrapper;
  }

  private static class Context {
    private final MarkupService service;
    private final TextAnnotation contentWrapper;
    private final List<String> xmlIds;

    public Context(MarkupService service, Parameters parameters) {
      this.service = service;
      this.contentWrapper = parameters.contentWrapper;
      this.xmlIds = parameters.xmlIds;
    }

    public void wrapContent(TextAnnotation parentTextAnnotation) {
      contentWrapper.setDepth(parentTextAnnotation.getDepth() + 1);
      service.wrapContentInChildTextAnnotation(parentTextAnnotation, contentWrapper);
    }

    public boolean hasRelevantXmlId(TextAnnotation textAnnotation) {
      // Log.info("textAnnotation={}", textAnnotation);
      String xmlId = textAnnotation.getAttributes().get(TextUtil.XML_ID);
      return xmlIds.contains(xmlId);
    }

  }

  private CommandResponse commandResponse = new CommandResponse();
  private MarkupService service;

  @Inject
  public WrapContentInElementCommand(MarkupService service) {
    this.service = service;
  }

  @Override
  public CommandResponse runWith(Map<String, Object> parameterMap) {
    Parameters parameters = validateParameters(parameterMap);
    if (commandResponse.parametersAreValid()) {
      parameters.resourceViewIds.stream()//
          .map(ResourceViewId::getResourceId)//
          .forEach(resourceId -> service.runInTransaction(() -> {
            Context context = new Context(service, parameters);
            service.getTextAnnotationStream(resourceId)//
                .filter(context::hasRelevantXmlId)//
                .forEach(context::wrapContent);
          }));
    }
    return commandResponse;
  }

  @SuppressWarnings("unchecked")
  private Parameters validateParameters(Map<String, Object> parameterMap) {
    Parameters parameters = new Parameters();
    parameters.resourceViewIds = validateResourceViewIds(parameterMap, commandResponse, service);
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
      parameters.contentWrapper = new TextAnnotation(name, attributes, 0);

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
