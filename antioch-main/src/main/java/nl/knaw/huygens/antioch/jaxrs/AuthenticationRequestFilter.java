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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.knaw.huygens.antioch.api.ApiConstants;

@Priority(Priorities.AUTHENTICATION)
public class AuthenticationRequestFilter implements ContainerRequestFilter {
  private static final Logger LOG = LoggerFactory.getLogger(AuthenticationRequestFilter.class);
  private static final String CLIENT_CERT_COMMON_NAME = "x-ssl-client-s-dn-cn";

  private final AntiochSecurityContextFactory securityContextFactory;

  @Inject
  public AuthenticationRequestFilter(AntiochSecurityContextFactory securityContextFactory) {
    this.securityContextFactory = securityContextFactory;
  }

  @Override
  public void filter(ContainerRequestContext requestContext) throws IOException {
    monitorExistingContext(requestContext);

    final UriInfo uriInfo = requestContext.getUriInfo();
    final URI requestURI = uriInfo.getRequestUri();

    final String certName = requestContext.getHeaderString(CLIENT_CERT_COMMON_NAME);
    final String authHeader = requestContext.getHeaderString(ApiConstants.HEADER_AUTH);
    LOG.trace("certName: [{}], authHeader: [{}]", certName, authHeader);

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

    if (LOG.isDebugEnabled()) {
      LOG.debug("Security context is: {}", requestContext.getSecurityContext());
    }
  }

  private void monitorExistingContext(ContainerRequestContext requestContext) {
    final SecurityContext securityContext = requestContext.getSecurityContext();
    if (securityContext == null) {
      LOG.warn("No pre-existing security context which should have been set by Jersey.");
    } else {
      LOG.debug("Overriding existing SecurityContext: [{}]", securityContext);

      final Principal principal = securityContext.getUserPrincipal();
      if (principal != null) {
        LOG.warn("Overriding existing principal: [{}]", principal);
      }
    }
  }
}
