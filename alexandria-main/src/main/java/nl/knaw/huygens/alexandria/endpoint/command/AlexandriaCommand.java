package nl.knaw.huygens.alexandria.endpoint.command;

import java.util.Map;

public interface AlexandriaCommand {
  public String getName();

  public CommandResponse runWith(Map<String, Object> parameterMap);
}
