package nl.knaw.huygens.alexandria.service;

/*
 * #%L
 * alexandria-main
 * =======
 * Copyright (C) 2015 - 2017 Huygens ING (KNAW)
 * =======
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.time.temporal.TemporalAmount;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

import nl.knaw.huygens.alexandria.api.model.AlexandriaState;
import nl.knaw.huygens.alexandria.api.model.search.AlexandriaQuery;
import nl.knaw.huygens.alexandria.endpoint.search.SearchResult;
import nl.knaw.huygens.alexandria.model.Accountable;
import nl.knaw.huygens.alexandria.model.AlexandriaAnnotation;
import nl.knaw.huygens.alexandria.model.AlexandriaAnnotationBody;
import nl.knaw.huygens.alexandria.model.AlexandriaResource;
import nl.knaw.huygens.alexandria.model.IdentifiablePointer;
import nl.knaw.huygens.alexandria.model.TentativeAlexandriaProvenance;
import nl.knaw.huygens.alexandria.textlocator.AlexandriaTextLocator;

public interface AlexandriaService {
  // NOTE: should these service methods all be atomic?

  /**
   * @return true if the resource was created, false if it was updated
   */
  boolean createOrUpdateResource(UUID uuid, String ref, TentativeAlexandriaProvenance provenance, AlexandriaState alexandriaState);

  AlexandriaResource createResource(UUID resourceUUID, String resourceRef, TentativeAlexandriaProvenance provenance, AlexandriaState confirmed);

  AlexandriaResource createSubResource(UUID uuid, UUID parentUuid, String sub, TentativeAlexandriaProvenance provenance);

  Optional<AlexandriaResource> readResource(UUID uuid);

  Optional<AlexandriaResource> readResourceWithUniqueRef(String resourceRef);

  List<AlexandriaResource> readSubResources(UUID uuid);

  AlexandriaAnnotationBody createAnnotationBody(UUID uuid, String type, String value, TentativeAlexandriaProvenance provenance);

  Optional<AlexandriaAnnotationBody> readAnnotationBody(UUID uuid);

  AlexandriaAnnotation annotate(AlexandriaResource resource, AlexandriaAnnotationBody annotationbody, TentativeAlexandriaProvenance provenance);

  AlexandriaAnnotation annotate(AlexandriaResource resource, AlexandriaTextLocator textLocator, AlexandriaAnnotationBody annotationbody, TentativeAlexandriaProvenance provenance);

  AlexandriaAnnotation annotate(AlexandriaAnnotation annotation, AlexandriaAnnotationBody annotationbody, TentativeAlexandriaProvenance provenance);

  Optional<AlexandriaAnnotation> readAnnotation(UUID uuid);

  Optional<AlexandriaAnnotation> readAnnotation(UUID uuid, Integer revision);

  Optional<? extends Accountable> dereference(IdentifiablePointer<? extends Accountable> pointer);

  /**
   * remove all unconfirmed objects that have timed out
   */
  void removeExpiredTentatives();

  TemporalAmount getTentativesTimeToLive();

  /**
   * @param oldAnnotationId the id of the {@link AlexandriaAnnotation} to deprecate
   * @param newAnnotation   the new {@link AlexandriaAnnotation}
   * @return the new {@link AlexandriaAnnotation} that deprecates the annotation with id oldAnnotationId
   */
  AlexandriaAnnotation deprecateAnnotation(UUID oldAnnotationId, AlexandriaAnnotation newAnnotation);

  void confirmResource(UUID id);

  void confirmAnnotation(UUID id);

  /**
   * If the annotation is TENTATIVE, it will be removed from the database.
   * Otherwise, its status will be set to DELETED
   *
   * @param annotation The Annotation to delete
   */
  void deleteAnnotation(AlexandriaAnnotation annotation);

  SearchResult execute(AlexandriaQuery query);

  // TODO: refactor these find methods to something more generic (search)
  Optional<AlexandriaResource> findSubresourceWithSubAndParentId(String sub, UUID parentId);

  Optional<AlexandriaAnnotationBody> findAnnotationBodyWithTypeAndValue(String type, String value);

  Map<String, Object> getMetadata();

  void destroy();

  void exportDb(String format, String filename);

  void importDb(String format, String filename);

  void runInTransaction(Runnable runner);

  <A> A runInTransaction(Supplier<A> supplier);

}
