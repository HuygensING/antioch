package nl.knaw.huygens.alexandria.endpoint.command;

import java.util.Map;

import nl.knaw.huygens.alexandria.api.model.CommandResponse;

public interface AlexandriaCommand {
  String getName();

  CommandResponse runWith(Map<String, Object> parameterMap);
}
