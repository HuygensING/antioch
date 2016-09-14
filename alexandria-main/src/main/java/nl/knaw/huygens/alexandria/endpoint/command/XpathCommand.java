package nl.knaw.huygens.alexandria.endpoint.command;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.io.output.StringBuilderWriter;
import org.apache.commons.io.output.WriterOutputStream;

import nl.knaw.huygens.alexandria.api.model.CommandResponse;
import nl.knaw.huygens.alexandria.api.model.Commands;
import nl.knaw.huygens.alexandria.service.AlexandriaService;
import nl.knaw.huygens.alexandria.textgraph.TextGraphUtil;
import nl.knaw.huygens.tei.QueryableDocument;

public class XpathCommand extends ResourcesCommand {
  private static final String PARAMETER_XPATH = "xpath";

  private static class Parameters {
    List<UUID> resourceIds;
    String xpath;
  }

  private CommandResponse commandResponse = new CommandResponse();
  private AlexandriaService service;

  @Inject
  public XpathCommand(AlexandriaService service) {
    this.service = service;
  }

  @Override
  public String getName() {
    return Commands.XPATH;
  }

  @Override
  public CommandResponse runWith(Map<String, Object> parameterMap) {
    Parameters parameters = validateParameters(parameterMap);
    Map<String, String> resultMap = new HashMap<>();
    if (commandResponse.parametersAreValid()) {
      for (UUID resourceId : parameters.resourceIds) {
        service.runInTransaction(() -> {
          StreamingOutput xmlOutputStream = TextGraphUtil.xmlOutputStream(service, resourceId, "");
          StringBuilderWriter sbWriter = new StringBuilderWriter();
          WriterOutputStream writerOutputStream = new WriterOutputStream(sbWriter);
          try {
            xmlOutputStream.write(writerOutputStream);
            writerOutputStream.close();
            String xml = sbWriter.toString();
            QueryableDocument qDocument = QueryableDocument.createFromXml(xml, true);
            String result = qDocument.evaluateXPathToString(parameters.xpath);
            resultMap.put(resourceId.toString(), result);
          } catch (WebApplicationException | IOException | XPathExpressionException e) {
            commandResponse.addErrorLine(resourceId + ": " + e.getMessage());
            e.printStackTrace();
          }
        });
      }
    }
    commandResponse.setResult(resultMap);
    return commandResponse;
  }

  private Parameters validateParameters(Map<String, Object> parameterMap) {
    final Parameters parameters = new Parameters();
    parameters.resourceIds = validateResourceIds(parameterMap, commandResponse, service);
    boolean valid = (commandResponse.getErrorLines().isEmpty());
    if (!parameterMap.containsKey(PARAMETER_XPATH)) {
      addXPathError();
      valid = false;

    } else {
      try {
        parameters.xpath = (String) parameterMap.get(PARAMETER_XPATH);
      } catch (ClassCastException e) {
        addXPathError();
        valid = false;
      }
    }

    if (valid) {
      commandResponse.setParametersAreValid(true);
    }
    return parameters;
  }

  private void addXPathError() {
    commandResponse.addErrorLine("Parameter '" + PARAMETER_XPATH + "' should be a valid xpath query.");
  }

}
