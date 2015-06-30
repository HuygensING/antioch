package nl.knaw.huygens.alexandria.concordion.commands;

import nl.knaw.huygens.alexandria.concordion.HuygensCommand;

@HuygensCommand(name = "get")
public class HttpGetCommand extends HttpMethodCommand {
  public HttpGetCommand() {
    super("GET");
  }
}
