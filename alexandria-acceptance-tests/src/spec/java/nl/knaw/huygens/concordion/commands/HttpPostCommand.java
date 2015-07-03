package nl.knaw.huygens.concordion.commands;

import nl.knaw.huygens.concordion.HuygensCommand;

@HuygensCommand(name = "post")
public class HttpPostCommand extends HttpMethodCommand {
  public HttpPostCommand() {
    super("POST");
  }
}
