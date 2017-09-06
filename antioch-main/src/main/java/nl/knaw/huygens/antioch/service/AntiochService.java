package nl.knaw.huygens.antioch.service;

/*
 * #%L
 * antioch-main
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

import nl.knaw.huygens.antioch.api.model.AntiochState;
import nl.knaw.huygens.antioch.api.model.search.AntiochQuery;
import nl.knaw.huygens.antioch.endpoint.search.SearchResult;
import nl.knaw.huygens.antioch.model.Accountable;
import nl.knaw.huygens.antioch.model.AntiochAnnotation;
import nl.knaw.huygens.antioch.model.AntiochAnnotationBody;
import nl.knaw.huygens.antioch.model.AntiochResource;
import nl.knaw.huygens.antioch.model.IdentifiablePointer;
import nl.knaw.huygens.antioch.model.TentativeAntiochProvenance;
import nl.knaw.huygens.antioch.textlocator.AntiochTextLocator;

public interface AntiochService {
  // NOTE: should these service methods all be atomic?

  /**
   * @return true if the resource was created, false if it was updated
   */
  boolean createOrUpdateResource(UUID uuid, String ref, TentativeAntiochProvenance provenance, AntiochState antiochState);

  AntiochResource createResource(UUID resourceUUID, String resourceRef, TentativeAntiochProvenance provenance, AntiochState confirmed);

  AntiochResource createSubResource(UUID uuid, UUID parentUuid, String sub, TentativeAntiochProvenance provenance);

  Optional<AntiochResource> readResource(UUID uuid);

  Optional<AntiochResource> readResourceWithUniqueRef(String resourceRef);

  List<AntiochResource> readSubResources(UUID uuid);

  AntiochAnnotationBody createAnnotationBody(UUID uuid, String type, String value, TentativeAntiochProvenance provenance);

  Optional<AntiochAnnotationBody> readAnnotationBody(UUID uuid);

  AntiochAnnotation annotate(AntiochResource resource, AntiochAnnotationBody annotationbody, TentativeAntiochProvenance provenance);

  AntiochAnnotation annotate(AntiochResource resource, AntiochTextLocator textLocator, AntiochAnnotationBody annotationbody, TentativeAntiochProvenance provenance);

  AntiochAnnotation annotate(AntiochAnnotation annotation, AntiochAnnotationBody annotationbody, TentativeAntiochProvenance provenance);

  Optional<AntiochAnnotation> readAnnotation(UUID uuid);

  Optional<AntiochAnnotation> readAnnotation(UUID uuid, Integer revision);

  Optional<? extends Accountable> dereference(IdentifiablePointer<? extends Accountable> pointer);

  /**
   * remove all unconfirmed objects that have timed out
   */
  void removeExpiredTentatives();

  TemporalAmount getTentativesTimeToLive();

  /**
   * @param oldAnnotationId the id of the {@link AntiochAnnotation} to deprecate
   * @param newAnnotation   the new {@link AntiochAnnotation}
   * @return the new {@link AntiochAnnotation} that deprecates the annotation with id oldAnnotationId
   */
  AntiochAnnotation deprecateAnnotation(UUID oldAnnotationId, AntiochAnnotation newAnnotation);

  void confirmResource(UUID id);

  void confirmAnnotation(UUID id);

  /**
   * If the annotation is TENTATIVE, it will be removed from the database.
   * Otherwise, its status will be set to DELETED
   *
   * @param annotation The Annotation to delete
   */
  void deleteAnnotation(AntiochAnnotation annotation);

  SearchResult execute(AntiochQuery query);

  // TODO: refactor these find methods to something more generic (search)
  Optional<AntiochResource> findSubresourceWithSubAndParentId(String sub, UUID parentId);

  Optional<AntiochAnnotationBody> findAnnotationBodyWithTypeAndValue(String type, String value);

  Map<String, Object> getMetadata();

  void destroy();

  void exportDb(String format, String filename);

  void importDb(String format, String filename);

  void runInTransaction(Runnable runner);

  <A> A runInTransaction(Supplier<A> supplier);

}
