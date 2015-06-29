package nl.knaw.huygens.alexandria.helpers.commands;

import nl.knaw.huygens.alexandria.helpers.HuygensConcordionCommand;

@HuygensConcordionCommand(command = "get")
public class HttpGetCommand extends HttpMethodCommand {
  public HttpGetCommand() {
    super("GET");
  }
}
