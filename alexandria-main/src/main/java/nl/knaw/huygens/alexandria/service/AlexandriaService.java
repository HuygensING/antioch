package nl.knaw.huygens.alexandria.service;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import nl.knaw.huygens.alexandria.model.Accountable;
import nl.knaw.huygens.alexandria.model.AccountablePointer;
import nl.knaw.huygens.alexandria.model.AlexandriaAnnotation;
import nl.knaw.huygens.alexandria.model.AlexandriaAnnotationBody;
import nl.knaw.huygens.alexandria.model.AlexandriaResource;
import nl.knaw.huygens.alexandria.model.AlexandriaState;
import nl.knaw.huygens.alexandria.model.TentativeAlexandriaProvenance;

public interface AlexandriaService {
  // NOTE: should these service methods all be atomic?
  /**
   *
   * @param uuid
   * @param ref
   * @param provenance
   * @param alexandriaState
   * @return true if the resource was created, false if it was updated
   */
  boolean createOrUpdateResource(UUID uuid, String ref, TentativeAlexandriaProvenance provenance, AlexandriaState alexandriaState);

  // boolean createOrUpdateAnnotation(UUID uuid, AlexandriaAnnotationBody annotationbody, TentativeAlexandriaProvenance provenance, AlexandriaState alexandriaState);

  AlexandriaResource createSubResource(UUID uuid, UUID parentUuid, String sub, TentativeAlexandriaProvenance provenance, AlexandriaState alexandriaState);

  Optional<AlexandriaResource> readResource(UUID uuid);

  Set<AlexandriaResource> readSubResources(UUID uuid);

  AlexandriaAnnotationBody createAnnotationBody(UUID uuid, Optional<String> type, String value, TentativeAlexandriaProvenance provenance, AlexandriaState alexandriaState);

  Optional<AlexandriaAnnotationBody> findAnnotationBodyWithTypeAndValue(Optional<String> type, String value);

  Optional<AlexandriaAnnotationBody> readAnnotationBody(UUID uuid);

  AlexandriaAnnotation annotate(AlexandriaResource resource, AlexandriaAnnotationBody annotationbody, TentativeAlexandriaProvenance provenance);

  AlexandriaAnnotation annotate(AlexandriaAnnotation annotation, AlexandriaAnnotationBody annotationbody, TentativeAlexandriaProvenance provenance);

  Optional<AlexandriaAnnotation> readAnnotation(UUID uuid);

  Optional<? extends Accountable> dereference(AccountablePointer<? extends Accountable> pointer);

  /**
   * remove all unconfirmed objects that have timed out
   */
  void removeExpiredTentatives();

}
