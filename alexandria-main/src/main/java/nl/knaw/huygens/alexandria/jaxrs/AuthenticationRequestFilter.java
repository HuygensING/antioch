package nl.knaw.huygens.alexandria.jaxrs;

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

@Priority(Priorities.AUTHENTICATION)
public class AuthenticationRequestFilter implements ContainerRequestFilter {
  private static final String AUTH_HEADER = "Auth";
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
    final String authHeader = requestContext.getHeaderString(AUTH_HEADER);
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
