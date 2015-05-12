package nl.knaw.huygens.alexandria.service;

import java.util.Optional;
import java.util.UUID;

import nl.knaw.huygens.alexandria.exception.IllegalResourceException;
import nl.knaw.huygens.alexandria.exception.NotFoundException;
import nl.knaw.huygens.alexandria.exception.ResourceExistsException;
import nl.knaw.huygens.alexandria.model.AlexandriaAnnotation;
import nl.knaw.huygens.alexandria.model.AlexandriaResource;

public interface AlexandriaService {
  AlexandriaResource createResource(UUID uuid) throws IllegalResourceException;

  AlexandriaResource readResource(UUID uuid) throws NotFoundException;

  AlexandriaResource updateResource(AlexandriaResource resource) throws IllegalResourceException;

  void deleteResource(UUID uuid) throws NotFoundException;

  AlexandriaAnnotation createAnnotation(UUID uuid, String type, Optional<String> value) throws ResourceExistsException;

  AlexandriaAnnotation readAnnotation(UUID uuid) throws NotFoundException;
}
