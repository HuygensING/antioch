package nl.knaw.huygens.alexandria.service;

import java.util.UUID;

import nl.knaw.huygens.alexandria.exception.IllegalResourceException;
import nl.knaw.huygens.alexandria.exception.ResourceExistsException;
import nl.knaw.huygens.alexandria.model.AlexandriaResource;

public interface ResourceService {
  AlexandriaResource createResource(AlexandriaResource protoType);

  void createResource(UUID uuid, String ref) throws ResourceExistsException;

  void createResource(String id, String ref) throws IllegalResourceException, ResourceExistsException;

  String getResource(String id) throws IllegalResourceException;
}
