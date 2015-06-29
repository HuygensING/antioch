package nl.knaw.huygens.alexandria.helpers.commands;

import nl.knaw.huygens.alexandria.helpers.HuygensConcordionCommand;

@HuygensConcordionCommand(command = "post")
public class HttpPostCommand extends HttpMethodCommand {
  public HttpPostCommand() {
    super("POST");
  }
}
