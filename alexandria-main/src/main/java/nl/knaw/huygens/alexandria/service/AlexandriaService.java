package nl.knaw.huygens.alexandria.service;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import nl.knaw.huygens.alexandria.endpoint.search.AlexandriaQuery;
import nl.knaw.huygens.alexandria.endpoint.search.SearchResult;
import nl.knaw.huygens.alexandria.model.Accountable;
import nl.knaw.huygens.alexandria.model.AlexandriaAnnotation;
import nl.knaw.huygens.alexandria.model.AlexandriaAnnotationBody;
import nl.knaw.huygens.alexandria.model.AlexandriaResource;
import nl.knaw.huygens.alexandria.model.AlexandriaState;
import nl.knaw.huygens.alexandria.model.IdentifiablePointer;
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

  AlexandriaAnnotationBody createAnnotationBody(UUID uuid, String type, String value, TentativeAlexandriaProvenance provenance, AlexandriaState alexandriaState);

  Optional<AlexandriaAnnotationBody> readAnnotationBody(UUID uuid);

  AlexandriaAnnotation annotate(AlexandriaResource resource, AlexandriaAnnotationBody annotationbody, TentativeAlexandriaProvenance provenance);

  AlexandriaAnnotation annotate(AlexandriaAnnotation annotation, AlexandriaAnnotationBody annotationbody, TentativeAlexandriaProvenance provenance);

  Optional<AlexandriaAnnotation> readAnnotation(UUID uuid);

  Optional<AlexandriaAnnotation> readAnnotation(UUID uuid, Integer revision);

  Optional<? extends Accountable> dereference(IdentifiablePointer<? extends Accountable> pointer);

  /**
   * remove all unconfirmed objects that have timed out
   */
  void removeExpiredTentatives();

  /**
   * @param oldAnnotationId
   *          the id of the {@link AlexandriaAnnotation} to deprecate
   * @param newAnnotation
   *          the new {@link AlexandriaAnnotation}
   * @return the new {@link AlexandriaAnnotation} that deprecates the annotation with id oldAnnotationId
   */
  AlexandriaAnnotation deprecateAnnotation(UUID oldAnnotationId, AlexandriaAnnotation newAnnotation);

  void confirmResource(UUID id);

  void confirmAnnotation(UUID id);

  /**
   * If the annotation is TENTATIVE, it will be removed from the database.
   * Otherwise, its status will be set to DELETED
   *
   * @param annotation
   *          The Annotation to delete
   */
  void deleteAnnotation(AlexandriaAnnotation annotation);

  SearchResult execute(AlexandriaQuery query);

  // TODO: refactor these find methods to something more generic (search)
  Optional<AlexandriaResource> findSubresourceWithSubAndParentId(String sub, UUID parentId);

  Optional<AlexandriaAnnotationBody> findAnnotationBodyWithTypeAndValue(String type, String value);

}
