package nl.knaw.huygens.alexandria.concordion.commands;

import nl.knaw.huygens.alexandria.concordion.HuygensCommand;

@HuygensCommand(name = "post")
public class HttpPostCommand extends HttpMethodCommand {
  public HttpPostCommand() {
    super("POST");
  }
}
