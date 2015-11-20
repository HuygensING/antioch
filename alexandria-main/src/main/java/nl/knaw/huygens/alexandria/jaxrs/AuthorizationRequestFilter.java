package nl.knaw.huygens.alexandria.jaxrs;

import java.io.IOException;
import java.util.Set;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import com.google.common.collect.Sets;

import nl.knaw.huygens.Log;
import nl.knaw.huygens.alexandria.jaxrs.Annotations.AuthorizationRequired;

@Priority(Priorities.AUTHORIZATION)
public class AuthorizationRequestFilter implements ContainerRequestFilter {
  private static final String AUTH_HEADER = "Auth";
  private static final Class<AuthorizationRequired> ANNOTATION_CLASS = AuthorizationRequired.class;
  private AlexandriaSecurityContextFactory securityContextFactory;

  @Context
  private ResourceInfo resourceInfo;

  @Inject
  public AuthorizationRequestFilter(AlexandriaSecurityContextFactory securityContextFactory) {
    this.securityContextFactory = securityContextFactory;
  }

  Set<String> PUBLIC_METHODS = Sets.newHashSet("GET", "HEAD", "OPTIONS");

  @Override
  public void filter(ContainerRequestContext requestContext) throws IOException {
    boolean needsAuth = resourceInfo.getResourceMethod().getAnnotation(ANNOTATION_CLASS) != null;
    if (needsAuth || !PUBLIC_METHODS.contains(requestContext.getMethod())) {
      String headerString = requestContext.getHeaderString(AUTH_HEADER);
      requestContext.setSecurityContext(securityContextFactory.createFrom(headerString));
      final SecurityContext securityContext = requestContext.getSecurityContext();
      if (securityContext == null || securityContext.getUserPrincipal() == null) {
        requestContext.abortWith(//
            Response.status(Response.Status.UNAUTHORIZED)//
                .entity("User cannot access the resource.")//
                .build());
      } else {
        Log.info("user={}", securityContext.getUserPrincipal().getName());
      }
    }
  }
}