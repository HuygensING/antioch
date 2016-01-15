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
