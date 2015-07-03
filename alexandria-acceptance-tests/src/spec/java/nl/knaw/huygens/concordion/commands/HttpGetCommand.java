package nl.knaw.huygens.concordion.commands;

import nl.knaw.huygens.concordion.HuygensCommand;

@HuygensCommand(name = "get")
public class HttpGetCommand extends HttpMethodCommand {
  public HttpGetCommand() {
    super("GET");
  }
}
