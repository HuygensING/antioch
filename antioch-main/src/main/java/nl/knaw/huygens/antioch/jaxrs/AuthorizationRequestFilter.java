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

import com.google.common.collect.Sets;

import javax.annotation.Priority;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.SecurityContext;
import java.util.Set;

import static javax.ws.rs.HttpMethod.*;

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

    return securityContext.isUserInRole(AntiochRoles.ANONYMOUS);
  }
}
