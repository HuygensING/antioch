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

import nl.knaw.huygens.Log;
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
    Log.trace("Setting default user name to: [{}]", userName);
    ThreadContext.setUserName(userName);
  }

}
