package nl.knaw.huygens.alexandria.service;

/*
 * #%L
 * alexandria-main
 * =======
 * Copyright (C) 2015 - 2016 Huygens ING (KNAW)
 * =======
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import java.time.temporal.TemporalAmount;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import nl.knaw.huygens.alexandria.api.model.AlexandriaState;
import nl.knaw.huygens.alexandria.api.model.Annotator;
import nl.knaw.huygens.alexandria.api.model.AnnotatorList;
import nl.knaw.huygens.alexandria.api.model.search.AlexandriaQuery;
import nl.knaw.huygens.alexandria.api.model.text.TextRangeAnnotation;
import nl.knaw.huygens.alexandria.api.model.text.view.TextView;
import nl.knaw.huygens.alexandria.api.model.text.view.TextViewDefinition;
import nl.knaw.huygens.alexandria.endpoint.search.SearchResult;
import nl.knaw.huygens.alexandria.model.Accountable;
import nl.knaw.huygens.alexandria.model.AlexandriaAnnotation;
import nl.knaw.huygens.alexandria.model.AlexandriaAnnotationBody;
import nl.knaw.huygens.alexandria.model.AlexandriaResource;
import nl.knaw.huygens.alexandria.model.IdentifiablePointer;
import nl.knaw.huygens.alexandria.model.TentativeAlexandriaProvenance;
import nl.knaw.huygens.alexandria.textgraph.ParseResult;
import nl.knaw.huygens.alexandria.textgraph.TextAnnotation;
import nl.knaw.huygens.alexandria.textgraph.TextGraphSegment;
import nl.knaw.huygens.alexandria.textlocator.AlexandriaTextLocator;

public interface AlexandriaService {
  // NOTE: should these service methods all be atomic?
  /**
   *
   * @return true if the resource was created, false if it was updated
   */
  boolean createOrUpdateResource(UUID uuid, String ref, TentativeAlexandriaProvenance provenance, AlexandriaState alexandriaState);

  AlexandriaResource createSubResource(UUID uuid, UUID parentUuid, String sub, TentativeAlexandriaProvenance provenance);

  Optional<AlexandriaResource> readResource(UUID uuid);

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

  Map<String, Object> getMetadata();

  void destroy();

  void exportDb(String format, String filename);

  void importDb(String format, String filename);

  void setResourceAnnotator(UUID resourceUUID, Annotator annotator);

  Optional<Annotator> readResourceAnnotator(UUID resourceUUID, String annotatorCode);

  AnnotatorList readResourceAnnotators(UUID id);

  void setTextRangeAnnotation(UUID resourceUUID, TextRangeAnnotation annotation);

  Optional<TextRangeAnnotation> readTextRangeAnnotation(UUID resourceUUID, UUID annotationUUID);

  boolean overlapsWithExisitingTextRangeAnnotationForResource(TextRangeAnnotation annotation, UUID resourceUUID);

  void setTextView(UUID resourceUUID, String viewId, TextView textView, TextViewDefinition textViewDefinition);

  Optional<TextViewDefinition> getTextViewDefinition(UUID resourceId, String viewId);

  Optional<TextView> getTextView(UUID resourceId, String view);

  /**
   * Gets the textviews for the resource and all its ancestors
   */
  List<TextView> getTextViewsForResource(UUID resourceUUID);

  boolean storeTextGraph(UUID resourceId, ParseResult result);

  Stream<TextGraphSegment> getTextGraphSegmentStream(UUID resourceId);

  void runInTransaction(Runnable runner);

  Stream<TextAnnotation> getTextAnnotationStream(UUID resourceId);

  void updateTextAnnotation(TextAnnotation textAnnotation);

  void wrapContentInChildTextAnnotation(TextAnnotation existingTextAnnotation, TextAnnotation newChildTextAnnotation);

}
