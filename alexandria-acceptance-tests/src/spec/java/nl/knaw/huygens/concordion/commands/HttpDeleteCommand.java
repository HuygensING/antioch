package nl.knaw.huygens.concordion.commands;

import nl.knaw.huygens.concordion.HuygensCommand;

@HuygensCommand(name = "delete")
public class HttpDeleteCommand extends HttpMethodCommand {
  public HttpDeleteCommand() {
    super("DELETE");
  }
}
