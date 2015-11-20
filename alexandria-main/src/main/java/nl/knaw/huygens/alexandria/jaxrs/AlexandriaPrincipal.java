package nl.knaw.huygens.alexandria.jaxrs;

import java.security.Principal;

public class AlexandriaPrincipal implements Principal {
  private String name;

  public AlexandriaPrincipal(String name) {
    this.name = name;
  }

  @Override
  public String getName() {
    return name;
  }

}
