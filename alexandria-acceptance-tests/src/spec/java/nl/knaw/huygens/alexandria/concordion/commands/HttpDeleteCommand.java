package nl.knaw.huygens.alexandria.concordion.commands;

import nl.knaw.huygens.alexandria.concordion.HuygensCommand;

@HuygensCommand(name = "delete")
public class HttpDeleteCommand extends HttpMethodCommand {
  public HttpDeleteCommand() {
    super("DELETE");
  }
}
