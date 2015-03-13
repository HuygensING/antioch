package nl.knaw.huygens.alexandria.service;

import nl.knaw.huygens.alexandria.exception.IllegalResourceException;
import nl.knaw.huygens.alexandria.model.AlexandriaResource;

public interface ResourceService {
  AlexandriaResource createResource(AlexandriaResource protoType);

  String getResource(String id) throws IllegalResourceException;
}
