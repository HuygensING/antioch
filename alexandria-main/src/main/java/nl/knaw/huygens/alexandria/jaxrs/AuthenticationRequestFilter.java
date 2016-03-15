package nl.knaw.huygens.alexandria.jaxrs;

/*
 * #%L
 * alexandria-main
 * =======
 * Copyright (C) 2015 - 2016 Huygens ING (KNAW)
 * =======
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import java.io.IOException;
import java.net.URI;
import java.security.Principal;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang3.StringUtils;

import nl.knaw.huygens.Log;
import nl.knaw.huygens.alexandria.api.ApiConstants;

@Priority(Priorities.AUTHENTICATION)
public class AuthenticationRequestFilter implements ContainerRequestFilter {
  private static final String CLIENT_CERT_COMMON_NAME = "x-ssl-client-s-dn-cn";

  private final AlexandriaSecurityContextFactory securityContextFactory;

  @Inject
  public AuthenticationRequestFilter(AlexandriaSecurityContextFactory securityContextFactory) {
    this.securityContextFactory = securityContextFactory;
  }

  @Override
  public void filter(ContainerRequestContext requestContext) throws IOException {
    monitorExistingContext(requestContext);

    final UriInfo uriInfo = requestContext.getUriInfo();
    final URI requestURI = uriInfo.getRequestUri();

    final String certName = requestContext.getHeaderString(CLIENT_CERT_COMMON_NAME);
    final String authHeader = requestContext.getHeaderString(ApiConstants.HEADER_AUTH);
    Log.trace("certName: [{}], authHeader: [{}]", certName, authHeader);

    // Try and prefer authentication methods in order: Certificate > Auth Header > Anonymous
    final SecurityContext securityContext;
    if (!StringUtils.isEmpty(certName)) {
      securityContext = securityContextFactory.fromCertificate(requestURI, certName);
    } else if (!StringUtils.isEmpty(authHeader)) {
      securityContext = securityContextFactory.fromAuthHeader(requestURI, authHeader);
    } else {
      securityContext = securityContextFactory.anonymous(requestURI);
    }
    requestContext.setSecurityContext(securityContext);

    if (Log.isDebugEnabled()) {
      Log.debug("Security context is: {}", requestContext.getSecurityContext());
    }
  }

  private void monitorExistingContext(ContainerRequestContext requestContext) {
    final SecurityContext securityContext = requestContext.getSecurityContext();
    if (securityContext == null) {
      Log.warn("No pre-existing security context which should have been set by Jersey.");
    } else {
      Log.debug("Overriding existing SecurityContext: [{}]", securityContext);

      final Principal principal = securityContext.getUserPrincipal();
      if (principal != null) {
        Log.warn("Overriding existing principal: [{}]", principal);
      }
    }
  }
}
