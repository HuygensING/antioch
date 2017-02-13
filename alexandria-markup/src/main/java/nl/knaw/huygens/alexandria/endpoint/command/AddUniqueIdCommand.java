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
import static nl.knaw.huygens.alexandria.text.TextUtil.XML_ID;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import javax.inject.Inject;

import com.google.common.collect.Lists;

import nl.knaw.huygens.alexandria.api.model.CommandResponse;
import nl.knaw.huygens.alexandria.api.model.Commands;
import nl.knaw.huygens.alexandria.service.AlexandriaService;
import nl.knaw.huygens.alexandria.textgraph.TextAnnotation;

public class AddUniqueIdCommand extends TextAnnotationCommand {
  private static final String PARAMETER_ELEMENTS = "elements";

  private static class Parameters {
    List<ResourceViewId> resourceViewIds;
    List<String> elementNames;
  }

  private static class Context {
    private static final String XMLID_MARKER = "-";
    private final Map<String, AtomicLong> counters;
    private List<String> existingIds;
    private AlexandriaService service;

    public Context(AlexandriaService service) {
      this.service = service;
      this.counters = new HashMap<>();
      this.existingIds = Lists.newArrayList();
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
    if (commandResponse.parametersAreValid()) {
      parameters.resourceViewIds.stream()//
          .map(ResourceViewId::getResourceId)//
          .forEach(resourceId -> service.runInTransaction(() -> {
            Context context = new Context(service);
            context.existingIds = service.getTextAnnotationStream(resourceId)//
                .filter(this::hasXmlId)//
                .map(this::getXmlId)//
                .collect(toList());
            service.getTextAnnotationStream(resourceId)//
                .filter(ta -> textAnnotationHasRelevantName(parameters, ta))//
                .filter(ta -> !hasXmlId(ta))//
                .forEach(context::setXmlId);
          }));
    }
    return commandResponse;
  }

  @SuppressWarnings("unchecked")
  private Parameters validateParameters(Map<String, Object> parameterMap) {
    final Parameters parameters = new Parameters();
    parameters.resourceViewIds = validateResourceViewIds(parameterMap, commandResponse, service);
    boolean valid = (commandResponse.getErrorLines().isEmpty());
    if (!parameterMap.containsKey(PARAMETER_ELEMENTS)) {
      addElementsError();
      valid = false;

    } else {
      try {
        parameters.elementNames = (List<String>) parameterMap.get(PARAMETER_ELEMENTS);
      } catch (ClassCastException e) {
        addElementsError();
        valid = false;
      }
    }

    if (valid) {
      commandResponse.setParametersAreValid(true);
    }
    return parameters;
  }

  private CommandResponse addElementsError() {
    return commandResponse.addErrorLine("Parameter '" + PARAMETER_ELEMENTS + "' should be list of element names.");
  }

  private boolean textAnnotationHasRelevantName(Parameters parameters, TextAnnotation textAnnotation) {
    return parameters.elementNames.contains(textAnnotation.getName());
  }

  @Override
  public String getName() {
    return Commands.ADD_UNIQUE_ID;
  }

}
