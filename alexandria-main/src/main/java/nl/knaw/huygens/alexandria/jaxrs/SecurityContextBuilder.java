package nl.knaw.huygens.alexandria.jaxrs;

import java.net.URI;
import java.security.Principal;
import java.util.Arrays;
import java.util.List;

import javax.ws.rs.core.SecurityContext;

import com.google.common.base.MoreObjects;

public class SecurityContextBuilder {
  private final boolean isSecure;
  private String principalName;
  private String authenticationScheme;
  private String[] grantedRoles;

  private SecurityContextBuilder(boolean isSecure) {
    this.isSecure = isSecure;
  }

  public static SecurityContextBuilder targeting(URI requestURI) {
    return new SecurityContextBuilder(hasSecureScheme(requestURI));
  }

  private static boolean hasSecureScheme(URI requestUri) {
    return "https".equals(requestUri.getScheme());
  }

  public SecurityContextBuilder forPrincipalNamed(String name) {
    this.principalName = name;
    return this;
  }

  public SecurityContextBuilder forUnknownPrincipal() {
    principalName = "unknown";
    return this;
  }

  public SecurityContextBuilder withAuthenticationScheme(String scheme) {
    this.authenticationScheme = scheme;
    return this;
  }

  public SecurityContextBuilder granting(String... roles) {
    this.grantedRoles = roles;
    return this;
  }

  public AlexandriaSecurityContext build() {
    validate();
    return new AlexandriaSecurityContext(isSecure, authenticationScheme, principalName, grantedRoles);
  }

  private void validate() {
    if (principalName == null) {
      throw new IllegalStateException("Principal MUST have a name");
    }

    if (grantedRoles == null) {
      throw new IllegalStateException("Role(s) MUST be granted");
    }
  }

  private static class AlexandriaSecurityContext implements SecurityContext {
    private final AlexandriaPrincipal principal;
    private final boolean isSecure;
    private final String authenticationScheme;
    private final List<String> roles;

    private AlexandriaSecurityContext(boolean isSecure, String authenticationScheme, String name, String... roles) {
      this.isSecure = isSecure;
      this.authenticationScheme = authenticationScheme;
      this.principal = new AlexandriaPrincipal(name);
      this.roles = Arrays.asList(roles);
    }

    @Override
    public Principal getUserPrincipal() {
      return principal;
    }

    @Override
    public boolean isUserInRole(String role) {
      return roles.contains(role);
    }

    @Override
    public boolean isSecure() {
      return isSecure;
    }

    @Override
    public String getAuthenticationScheme() {
      return authenticationScheme;
    }

    @Override
    public String toString() {
      return MoreObjects.toStringHelper(this) //
                        .add("scheme", getAuthenticationScheme()) //
                        .add("isSecure", isSecure()) //
                        .add("principal", getUserPrincipal()) //
                        .add("roles", roles) //
                        .toString();
    }
  }

  private static class AlexandriaPrincipal implements Principal {
    private final String name;

    private AlexandriaPrincipal(String name) {
      this.name = name;
    }

    @Override
    public String getName() {
      return name;
    }

    @Override
    public String toString() {
      return MoreObjects.toStringHelper(this).add("name", getName()).toString();
    }
  }
}