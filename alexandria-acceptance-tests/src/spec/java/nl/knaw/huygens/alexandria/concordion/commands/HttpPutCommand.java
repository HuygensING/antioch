package nl.knaw.huygens.alexandria.concordion.commands;

import nl.knaw.huygens.alexandria.concordion.HuygensCommand;

@HuygensCommand(name = "put")
public class HttpPutCommand extends HttpMethodCommand {
  public HttpPutCommand() {
    super("PUT");
  }
}
