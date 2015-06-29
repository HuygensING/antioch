package nl.knaw.huygens.alexandria.helpers.commands;

import nl.knaw.huygens.alexandria.helpers.HuygensConcordionCommand;

@HuygensConcordionCommand(command = "put")
public class HttpPutCommand extends HttpMethodCommand {
  public HttpPutCommand() {
    super("PUT");
  }
}
