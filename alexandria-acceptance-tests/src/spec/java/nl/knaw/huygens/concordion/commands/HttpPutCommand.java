package nl.knaw.huygens.concordion.commands;

import nl.knaw.huygens.concordion.HuygensCommand;

@HuygensCommand(name = "put")
public class HttpPutCommand extends HttpMethodCommand {
  public HttpPutCommand() {
    super("PUT");
  }
}
