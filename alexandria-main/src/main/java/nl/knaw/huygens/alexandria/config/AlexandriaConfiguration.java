package nl.knaw.huygens.alexandria.config;

import java.net.URI;

public interface AlexandriaConfiguration {
  URI getBaseURI();

  String getStorageDirectory();
}
