package nl.knaw.huygens.antioch.jaxrs;

/*
 * #%L
 * antioch-main
 * =======
 * Copyright (C) 2015 - 2017 Huygens ING (KNAW)
 * =======
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

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

  public AntiochSecurityContext build() {
    validate();
    return new AntiochSecurityContext(isSecure, authenticationScheme, principalName, grantedRoles);
  }

  private void validate() {
    if (principalName == null) {
      throw new IllegalStateException("Principal MUST have a name");
    }

    if (grantedRoles == null) {
      throw new IllegalStateException("Role(s) MUST be granted");
    }
  }

  private static class AntiochSecurityContext implements SecurityContext {
    private final AntiochPrincipal principal;
    private final boolean isSecure;
    private final String authenticationScheme;
    private final List<String> roles;

    private AntiochSecurityContext(boolean isSecure, String authenticationScheme, String name, String... roles) {
      this.isSecure = isSecure;
      this.authenticationScheme = authenticationScheme;
      this.principal = new AntiochPrincipal(name);
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

  private static class AntiochPrincipal implements Principal {
    private final String name;

    private AntiochPrincipal(String name) {
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
