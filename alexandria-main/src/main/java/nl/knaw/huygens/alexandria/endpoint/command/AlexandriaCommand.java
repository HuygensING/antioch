package nl.knaw.huygens.alexandria.endpoint.command;

import java.util.Map;

import nl.knaw.huygens.alexandria.api.model.CommandResponse;

public interface AlexandriaCommand {
  public String getName();

  public CommandResponse runWith(Map<String, Object> parameterMap);
}
