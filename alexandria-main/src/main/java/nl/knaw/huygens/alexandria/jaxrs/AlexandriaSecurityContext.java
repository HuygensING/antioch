package nl.knaw.huygens.alexandria.jaxrs;

import java.security.Principal;

import javax.ws.rs.core.SecurityContext;

public class AlexandriaSecurityContext implements SecurityContext {
  private Principal principal;
  private String authenticationScheme;

  protected AlexandriaSecurityContext() {
  }

  protected AlexandriaSecurityContext withUserPrincipal(Principal principal) {
    this.principal = principal;
    return this;
  }

  protected AlexandriaSecurityContext withAuthenticationScheme(String authenticationScheme) {
    this.authenticationScheme = authenticationScheme;
    return this;
  }

  @Override
  public Principal getUserPrincipal() {
    return principal;
  }

  @Override
  public boolean isUserInRole(String role) {
    return false;
  }

  @Override
  public boolean isSecure() {
    return false;
  }

  @Override
  public String getAuthenticationScheme() {
    return authenticationScheme;
  }

}
