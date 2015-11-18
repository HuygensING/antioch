package nl.knaw.huygens.alexandria.jaxrs;

import java.util.Map;

import javax.inject.Inject;
import javax.ws.rs.core.SecurityContext;

import org.apache.commons.lang3.StringUtils;

import nl.knaw.huygens.alexandria.config.AlexandriaConfiguration;

public class AlexandriaSecurityContextFactory {

  private Map<String, String> keyMap; // authkey -> username

  @Inject
  public AlexandriaSecurityContextFactory(AlexandriaConfiguration config) {
    keyMap = config.getAuthKeyIndex();
  }

  // private static final String PREFIX = "authkey.";
  //
  // public Map<String, String> getAuthKeyIndex() {
  // PropertiesConfiguration properties = null;
  // return properties.getKeys().stream()//
  // .filter(k -> k.startsWith(PREFIX))//
  // .map(k -> k.replaceFirst(PREFIX, ""))//
  // .collect(toMap(name -> properties.getProperty(PREFIX + name).get(), name -> name));
  // }

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
