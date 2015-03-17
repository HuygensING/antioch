package nl.knaw.huygens.alexandria.service;

import java.util.Optional;
import java.util.UUID;

import nl.knaw.huygens.alexandria.exception.NotFoundException;
import nl.knaw.huygens.alexandria.model.AlexandriaAnnotation;

public interface AnnotationService {
  Optional<AlexandriaAnnotation> getAnnotation(UUID uuid) throws NotFoundException;
}
