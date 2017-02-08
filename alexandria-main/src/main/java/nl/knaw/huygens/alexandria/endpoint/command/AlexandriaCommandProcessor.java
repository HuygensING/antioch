package nl.knaw.huygens.alexandria.endpoint.command;

import java.util.Map;

import javax.inject.Inject;

import nl.knaw.huygens.alexandria.api.model.CommandResponse;
import nl.knaw.huygens.alexandria.api.model.Commands;
import nl.knaw.huygens.alexandria.service.AlexandriaService;

public class AlexandriaCommandProcessor {

  private AlexandriaService service;
  private AQL2Command aql2Command;

  @Inject
  public AlexandriaCommandProcessor(AlexandriaService service, AQL2Command aql2Command) {
    this.service = service;
    this.aql2Command = aql2Command;

  }

  public CommandResponse process(String command, Map<String, Object> parameterMap) {
    switch (command) {
    case Commands.ADD_UNIQUE_ID:
      return new AddUniqueIdCommand(service).runWith(parameterMap);
    case Commands.XPATH:
      return new XpathCommand(service).runWith(parameterMap);
    case Commands.AQL2:
      return aql2Command.runWith(parameterMap);

    // disable wrap-content-in-element command
    // case Commands.WRAP_CONTENT_IN_ELEMENT:
    // return new WrapContentInElementCommand(service).runWith(parameterMap);

    default:
      return new CommandResponse().addErrorLine("command '" + command + "' not known.");
    }

  }

}
