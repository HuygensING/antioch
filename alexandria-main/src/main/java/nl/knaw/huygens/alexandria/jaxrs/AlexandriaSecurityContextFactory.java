package nl.knaw.huygens.alexandria.jaxrs;

/*
 * #%L
 * alexandria-main
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

import static javax.ws.rs.core.SecurityContext.CLIENT_CERT_AUTH;
import static nl.knaw.huygens.alexandria.jaxrs.AlexandriaRoles.ANONYMOUS;
import static nl.knaw.huygens.alexandria.jaxrs.AlexandriaRoles.CERTIFIED;
import static nl.knaw.huygens.alexandria.jaxrs.AlexandriaRoles.JANITOR;

import java.net.URI;
import java.util.Map;

import javax.inject.Inject;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.core.SecurityContext;

import org.apache.commons.lang3.StringUtils;

import nl.knaw.huygens.alexandria.config.AlexandriaConfiguration;

public class AlexandriaSecurityContextFactory {
  private final Map<String, String> keyMap; // authKey -> username

  @Inject
  public AlexandriaSecurityContextFactory(AlexandriaConfiguration config) {
    keyMap = config.getAuthKeyIndex();
  }

  public SecurityContext fromCertificate(URI requestURI, String userName) {
    setDefaultUserName(userName);

    final SecurityContextBuilder builder = SecurityContextBuilder.targeting(requestURI);
    builder.withAuthenticationScheme(CLIENT_CERT_AUTH).forPrincipalNamed(userName).granting(CERTIFIED);
    return builder.build();
  }

  public SecurityContext fromAuthHeader(URI requestURI, String headerString) {
    if (StringUtils.isEmpty(headerString)) {
      notAuthorized();
    }

    final String[] parts = headerString.split(" ");
    if (parts.length != 2) {
      notAuthorized();
    }

    final String scheme = parts[0];
    final String authKey = parts[1];
    if (!keyMap.containsKey(authKey)) {
      notAuthorized();
    }

    final String userName = keyMap.get(authKey);
    setDefaultUserName(userName);

    final SecurityContextBuilder builder = SecurityContextBuilder.targeting(requestURI);
    builder.withAuthenticationScheme(scheme).forPrincipalNamed(userName).granting(JANITOR);
    return builder.build();
  }

  public SecurityContext anonymous(URI requestURI) {
    setDefaultUserName("unknown");
    return SecurityContextBuilder.targeting(requestURI).forUnknownPrincipal().granting(ANONYMOUS).build();
  }

  private void notAuthorized() {
    throw new NotAuthorizedException(""); // empty 'challenge'
  }

  private void setDefaultUserName(final String userName) {
    // TODO: refactor to get rid of saving userName in a thread local.
    // Log.trace("Setting default user name to: [{}]", userName);
    ThreadContext.setUserName(userName);
  }

}
