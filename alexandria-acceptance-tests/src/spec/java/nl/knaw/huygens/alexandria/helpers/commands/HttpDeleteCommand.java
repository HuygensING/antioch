package nl.knaw.huygens.alexandria.helpers.commands;

import nl.knaw.huygens.alexandria.helpers.HuygensConcordionCommand;

@HuygensConcordionCommand(command = "delete")
public class HttpDeleteCommand extends HttpMethodCommand {
  public HttpDeleteCommand() {
    super("DELETE");
  }
}
