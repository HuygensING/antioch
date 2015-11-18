package nl.knaw.huygens.alexandria.jaxrs;

import static java.util.stream.Collectors.toMap;

import java.util.Map;

import javax.inject.Inject;
import javax.ws.rs.core.SecurityContext;

import org.apache.commons.lang3.StringUtils;

import nl.knaw.huygens.alexandria.config.InstanceProperties;

public class AlexandriaSecurityContextFactory {

  private static final String PREFIX = "authkey.";
  private Map<String, String> keyMap; // authkey -> username

  @Inject
  public AlexandriaSecurityContextFactory(InstanceProperties config) {
    keyMap = config.getKeys().stream()//
        .filter(k -> k.startsWith(PREFIX))//
        .map(k -> k.replaceFirst(PREFIX, ""))//
        .collect(toMap(name -> config.getProperty(PREFIX + name).get(), name -> name));
  }

  public SecurityContext createFrom(String headerString) {
    if (StringUtils.isEmpty(headerString)) {
      return null;
    }

    String[] parts = headerString.split(" ");
    String authkey = parts[1];
    if (keyMap.containsKey(authkey)) {
      String name = keyMap.get(authkey);
      AlexandriaSecurityContext alexandriaSecurityContext = new AlexandriaSecurityContext()//
          .withAuthenticationScheme(parts[0])//
          .withUserPrincipal(new AlexandriaPrincipal(name));
      return alexandriaSecurityContext;
    }

    return null;
  }

}
