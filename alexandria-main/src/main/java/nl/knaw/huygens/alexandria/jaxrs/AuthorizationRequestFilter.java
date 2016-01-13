package nl.knaw.huygens.alexandria.jaxrs;

import static javax.ws.rs.HttpMethod.GET;
import static javax.ws.rs.HttpMethod.HEAD;
import static javax.ws.rs.HttpMethod.OPTIONS;

import java.util.Set;

import javax.annotation.Priority;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.SecurityContext;

import com.google.common.collect.Sets;

@Priority(Priorities.AUTHORIZATION)
public class AuthorizationRequestFilter implements ContainerRequestFilter {
  private static final Set<String> PUBLIC_HTTP_METHODS = Sets.newHashSet(GET, HEAD, OPTIONS);

  @Override
  public void filter(final ContainerRequestContext requestContext) {
    final boolean isAnonymous = isAnonymousUser(requestContext.getSecurityContext());
    final boolean methodAllowedForAnonymous = isPublicMethod(requestContext.getMethod());

    if (!methodAllowedForAnonymous && isAnonymous) {
      throw new ForbiddenException("Request for non-public HTTP method denied to anonymous user");
    }
  }

  private boolean isPublicMethod(String method) {
    return PUBLIC_HTTP_METHODS.contains(method);
  }

  private boolean isAnonymousUser(SecurityContext securityContext) {
    if (securityContext == null) {
      throw new ForbiddenException("Unable to determine security context.");
    }

    return securityContext.isUserInRole(AlexandriaRoles.ANONYMOUS);
  }
}