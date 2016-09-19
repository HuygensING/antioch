package nl.knaw.huygens.alexandria.endpoint.command;

import java.util.Map;

import javax.inject.Inject;

import nl.knaw.huygens.alexandria.api.model.CommandResponse;
import nl.knaw.huygens.alexandria.api.model.Commands;
import nl.knaw.huygens.alexandria.service.AlexandriaService;

public class AQL2Command extends ResourcesCommand {

  public static final String COMMAND_PARAMETER = "command";
  private CommandResponse commandResponse = new CommandResponse();
  private AlexandriaService service;

  @Inject
  public AQL2Command(AlexandriaService service) {
    this.service = service;
  }

  @Override
  public String getName() {
    return Commands.AQL2;
  }

  @Override
  public CommandResponse runWith(Map<String, Object> parameterMap) {
    String aql2Command = (String) parameterMap.get(COMMAND_PARAMETER);
    String result = process(aql2Command);
    commandResponse.setResult(result);
    commandResponse.setParametersAreValid(true);
    return commandResponse;
  }

  private String process(String aql2Command) {
    String result = "Command executed: " + aql2Command;
    return result;
  }

}
