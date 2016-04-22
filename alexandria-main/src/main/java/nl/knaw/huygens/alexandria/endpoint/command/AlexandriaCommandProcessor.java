package nl.knaw.huygens.alexandria.endpoint.command;

import java.util.Map;

import javax.inject.Inject;

import nl.knaw.huygens.alexandria.service.AlexandriaService;

public class AlexandriaCommandProcessor {

  private AlexandriaService service;

  @Inject
  public AlexandriaCommandProcessor(AlexandriaService service) {
    this.service = service;
  }

  public CommandResponse process(String command, Map<String, Object> parameterMap) {
    if (AddUniqueIdCommand.COMMAND_NAME.equals(command)) {
      return new AddUniqueIdCommand(service).runWith(parameterMap);
    }

    return new CommandResponse().addErrorLine("command '" + command + "' not known.");
  }

}
