package nl.knaw.huygens.alexandria.service;

/*
 * #%L
 * alexandria-service
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

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import java.io.IOException;
import java.io.OutputStream;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.TemporalAmount;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang3.StringUtils;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.jooq.lambda.Unchecked;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import nl.knaw.huygens.Log;
import nl.knaw.huygens.alexandria.api.model.AlexandriaState;
import nl.knaw.huygens.alexandria.api.model.Annotator;
import nl.knaw.huygens.alexandria.api.model.AnnotatorList;
import nl.knaw.huygens.alexandria.api.model.search.AlexandriaQuery;
import nl.knaw.huygens.alexandria.api.model.text.TextRangeAnnotation;
import nl.knaw.huygens.alexandria.api.model.text.TextRangeAnnotation.Position;
import nl.knaw.huygens.alexandria.api.model.text.view.TextView;
import nl.knaw.huygens.alexandria.api.model.text.view.TextViewDefinition;
import nl.knaw.huygens.alexandria.endpoint.LocationBuilder;
import nl.knaw.huygens.alexandria.endpoint.search.SearchResult;
import nl.knaw.huygens.alexandria.exception.BadRequestException;
import nl.knaw.huygens.alexandria.exception.NotFoundException;
import nl.knaw.huygens.alexandria.model.Accountable;
import nl.knaw.huygens.alexandria.model.AlexandriaAnnotation;
import nl.knaw.huygens.alexandria.model.AlexandriaAnnotationBody;
import nl.knaw.huygens.alexandria.model.AlexandriaProvenance;
import nl.knaw.huygens.alexandria.model.AlexandriaResource;
import nl.knaw.huygens.alexandria.model.IdentifiablePointer;
import nl.knaw.huygens.alexandria.model.TentativeAlexandriaProvenance;
import nl.knaw.huygens.alexandria.query.AlexandriaQueryParser;
import nl.knaw.huygens.alexandria.query.ParsedAlexandriaQuery;
import nl.knaw.huygens.alexandria.storage.DumpFormat;
import nl.knaw.huygens.alexandria.storage.Storage;
import nl.knaw.huygens.alexandria.storage.frames.AlexandriaVF;
import nl.knaw.huygens.alexandria.storage.frames.AnnotationBodyVF;
import nl.knaw.huygens.alexandria.storage.frames.AnnotationVF;
import nl.knaw.huygens.alexandria.storage.frames.AnnotatorVF;
import nl.knaw.huygens.alexandria.storage.frames.ResourceVF;
import nl.knaw.huygens.alexandria.storage.frames.TextRangeAnnotationVF;
import nl.knaw.huygens.alexandria.textgraph.ParseResult;
import nl.knaw.huygens.alexandria.textgraph.TextAnnotation;
import nl.knaw.huygens.alexandria.textgraph.TextGraphSegment;
import nl.knaw.huygens.alexandria.textlocator.AlexandriaTextLocator;
import nl.knaw.huygens.alexandria.textlocator.TextLocatorFactory;
import nl.knaw.huygens.alexandria.textlocator.TextLocatorParseException;
import nl.knaw.huygens.alexandria.util.StreamUtil;
import peapod.FramedGraphTraversal;

public class TinkerPopService implements AlexandriaService {
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().registerModule(new Jdk8Module());

  private static final TypeReference<Map<String, TextView>> TEXTVIEW_TYPEREF = new TypeReference<Map<String, TextView>>() {
  };
  private static final ObjectReader TEXTVIEW_READER = OBJECT_MAPPER.readerFor(TEXTVIEW_TYPEREF);
  private static final ObjectWriter TEXTVIEW_WRITER = OBJECT_MAPPER.writerFor(TEXTVIEW_TYPEREF);

  private static final TypeReference<Map<String, TextViewDefinition>> TEXTVIEWDEFINITION_TYPEREF = new TypeReference<Map<String, TextViewDefinition>>() {
  };
  private static final ObjectReader TEXTVIEWDEFINITION_READER = OBJECT_MAPPER.readerFor(TEXTVIEWDEFINITION_TYPEREF);
  private static final ObjectWriter TEXTVIEWDEFINITION_WRITER = OBJECT_MAPPER.writerFor(TEXTVIEWDEFINITION_TYPEREF);

  private static final TemporalAmount TENTATIVES_TTL = Duration.ofDays(1);

  private Storage storage;
  private LocationBuilder locationBuilder;
  private AlexandriaQueryParser alexandriaQueryParser;
  private TextGraphService textGraphService;

  @Inject
  public TinkerPopService(Storage storage, LocationBuilder locationBuilder) {
    Log.trace("{} created, locationBuilder=[{}]", getClass().getSimpleName(), locationBuilder);
    this.locationBuilder = locationBuilder;
    this.alexandriaQueryParser = new AlexandriaQueryParser(locationBuilder);
    setStorage(storage);
  }

  public void setStorage(Storage storage) {
    this.storage = storage;
    this.textGraphService = new TextGraphService(storage);
  }

  // - AlexandriaService methods -//
  // use storage.runInTransaction for transactions

  @Override
  public boolean createOrUpdateResource(UUID uuid, String ref, TentativeAlexandriaProvenance provenance, AlexandriaState state) {
    return storage.runInTransaction(() -> {
      AlexandriaResource resource;
      boolean result;

      if (storage.existsVF(ResourceVF.class, uuid)) {
        resource = getOptionalResource(uuid).get();
        result = false;

      } else {
        resource = new AlexandriaResource(uuid, provenance);
        result = true;
      }
      resource.setCargo(ref);
      resource.setState(state);
      createOrUpdateResource(resource);
      return result;
    });
  }

  private Optional<AlexandriaResource> getOptionalResource(UUID uuid) {
    return storage.readVF(ResourceVF.class, uuid).map(this::deframeResource);
  }

  @Override
  public AlexandriaAnnotation annotate(AlexandriaResource resource, AlexandriaAnnotationBody annotationbody, TentativeAlexandriaProvenance provenance) {
    AlexandriaAnnotation newAnnotation = createAnnotation(annotationbody, provenance);
    annotateResourceWithAnnotation(resource, newAnnotation);
    return newAnnotation;
  }

  @Override
  public AlexandriaAnnotation annotate(AlexandriaResource resource, AlexandriaTextLocator textLocator, AlexandriaAnnotationBody annotationbody, TentativeAlexandriaProvenance provenance) {
    AlexandriaAnnotation newAnnotation = createAnnotation(textLocator, annotationbody, provenance);
    annotateResourceWithAnnotation(resource, newAnnotation);
    return newAnnotation;
  }

  @Override
  public AlexandriaAnnotation annotate(AlexandriaAnnotation annotation, AlexandriaAnnotationBody annotationbody, TentativeAlexandriaProvenance provenance) {
    AlexandriaAnnotation newAnnotation = createAnnotation(annotationbody, provenance);
    annotateAnnotationWithAnnotation(annotation, newAnnotation);
    return newAnnotation;
  }

  @Override
  public AlexandriaResource createSubResource(UUID uuid, UUID parentUuid, String sub, TentativeAlexandriaProvenance provenance) {
    AlexandriaResource subresource = new AlexandriaResource(uuid, provenance);
    subresource.setCargo(sub);
    subresource.setParentResourcePointer(new IdentifiablePointer<>(AlexandriaResource.class, parentUuid.toString()));
    createSubResource(subresource);
    return subresource;
  }

  @Override
  public Optional<? extends Accountable> dereference(IdentifiablePointer<? extends Accountable> pointer) {
    Class<? extends Accountable> aClass = pointer.getIdentifiableClass();
    UUID uuid = UUID.fromString(pointer.getIdentifier());
    if (AlexandriaResource.class.equals(aClass)) {
      return readResource(uuid);

    } else if (AlexandriaAnnotation.class.equals(aClass)) {
      return readAnnotation(uuid);

    } else {
      throw new RuntimeException("unexpected accountableClass: " + aClass.getName());
    }
  }

  @Override
  public Optional<AlexandriaResource> readResource(UUID uuid) {
    return storage.runInTransaction(() -> getOptionalResource(uuid));
  }

  @Override
  public Optional<AlexandriaAnnotation> readAnnotation(UUID uuid) {
    return storage.runInTransaction(() -> storage.readVF(AnnotationVF.class, uuid).map(this::deframeAnnotation));
  }

  @Override
  public Optional<AlexandriaAnnotation> readAnnotation(UUID uuid, Integer revision) {
    return storage.runInTransaction(() -> {
      Optional<AnnotationVF> versionedAnnotation = storage.readVF(AnnotationVF.class, uuid, revision);
      if (versionedAnnotation.isPresent()) {
        return versionedAnnotation.map(this::deframeAnnotation);

      } else {
        Optional<AnnotationVF> currentAnnotation = storage.readVF(AnnotationVF.class, uuid);
        if (currentAnnotation.isPresent() && currentAnnotation.get().getRevision().equals(revision)) {
          return currentAnnotation.map(this::deframeAnnotation);
        } else {
          return Optional.empty();
        }
      }
    });
  }

  @Override
  public TemporalAmount getTentativesTimeToLive() {
    return TENTATIVES_TTL;
  }

  @Override
  public void removeExpiredTentatives() {
    // Tentative vertices should not have any outgoing or incoming edges!!
    Long threshold = Instant.now().minus(TENTATIVES_TTL).getEpochSecond();
    storage.runInTransaction(() -> storage.removeExpiredTentatives(threshold));
  }

  @Override
  public Optional<AlexandriaAnnotationBody> findAnnotationBodyWithTypeAndValue(String type, String value) {
    final List<AnnotationBodyVF> results = storage.runInTransaction(//
        () -> storage.find(AnnotationBodyVF.class)//
            .has("type", type)//
            .has("value", value)//
            .toList());
    if (results.isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(deframeAnnotationBody(results.get(0)));
  }

  @Override
  public Optional<AlexandriaResource> findSubresourceWithSubAndParentId(String sub, UUID parentId) {
    // TODO: find the gremlin way to do this in one:
    // in cypher: match (r:Resource{uuid:parentId})<-[:PART_OF]-(s:Resource{cargo:sub}) return s.uuid
    return storage.runInTransaction(//
        () -> storage.find(ResourceVF.class)//
            .has("cargo", sub)//
            .toList()//
            .parallelStream()//
            .filter(r -> r.getParentResource() != null//
                && r.getParentResource().getUuid().equals(parentId.toString()))//
            .map(this::deframeResource)//
            .findAny());
  }

  @Override
  public Set<AlexandriaResource> readSubResources(UUID uuid) {
    ResourceVF resourcevf = readExisitingResourceVF(uuid);
    return resourcevf.getSubResources().stream()//
        .map(this::deframeResource)//
        .collect(toSet());
  }

  @Override
  public void setResourceAnnotator(UUID resourceUUID, Annotator annotator) {
    storage.runInTransaction(() -> {
      // remove existing annotator for this resource with the same annotator code
      storage.getResourceVertexTraversal()//
          .has(Storage.IDENTIFIER_PROPERTY, resourceUUID.toString())//
          .in(AnnotatorVF.HAS_RESOURCE)//
          .has("code", annotator.getCode())//
          .toList()//
          .forEach(Vertex::remove);
      AnnotatorVF avf = frameAnnotator(annotator);
      ResourceVF resourceVF = storage.readVF(ResourceVF.class, resourceUUID).get();
      avf.setResource(resourceVF);
      Log.info("avf.resource={}", avf.getResource().getUuid());
    });

  }

  @Override
  public Optional<Annotator> readResourceAnnotator(UUID uuid, String code) {
    ResourceVF resourcevf = readExisitingResourceVF(uuid);
    return resourcevf.getAnnotators().parallelStream()//
        .map(this::deframeAnnotator)//
        .filter(a -> code.equals(a.getCode()))//
        .findAny();
  }

  @Override
  public AnnotatorList readResourceAnnotators(UUID uuid) {
    List<AnnotatorVF> annotatorVFs = storage.runInTransaction(() -> {
      ResourceVF resourceVF = readExisitingResourceVF(uuid);
      List<AnnotatorVF> annotatorVFList = Lists.newArrayList();
      do {
        annotatorVFList.addAll(resourceVF.getAnnotators());
        resourceVF = resourceVF.getParentResource();
      } while (resourceVF != null);

      return annotatorVFList;
    });
    AnnotatorList annotators = new AnnotatorList();
    Set<String> codes = Sets.newHashSet();
    annotatorVFs.stream().map(this::deframeAnnotator)//
        .forEach(a -> {
          if (!codes.contains(a.getCode())) {
            codes.add(a.getCode());
            annotators.add(a);
          }
        });
    return annotators;
  }

  @Override
  public void setTextRangeAnnotation(UUID resourceUUID, TextRangeAnnotation annotation) {
    storage.runInTransaction(() -> {
      TextRangeAnnotationVF vf = storage.readVF(TextRangeAnnotationVF.class, annotation.getId())//
          .orElseGet(() -> storage.createVF(TextRangeAnnotationVF.class));
      updateTextRangeAnnotation(vf, annotation);
      textGraphService.updateTextAnnotationLink(vf, annotation, resourceUUID);
      vf.setResource(storage.readVF(ResourceVF.class, resourceUUID).get());
    });
  }

  @Override
  public Optional<TextRangeAnnotation> readTextRangeAnnotation(UUID resourceUUID, UUID annotationUUID) {
    return storage.runInTransaction(() -> getOptionalTextRangeAnnotation(resourceUUID, annotationUUID));
  }

  private Optional<TextRangeAnnotation> getOptionalTextRangeAnnotation(UUID resourceUUID, UUID annotationUUID) {
    return storage.readVF(TextRangeAnnotationVF.class, annotationUUID).map(this::deframeTextRangeAnnotation);
  }

  @Override
  public boolean overlapsWithExisitingTextRangeAnnotationForResource(TextRangeAnnotation annotation, UUID resourceUUID) {
    return storage.runInTransaction(() -> {
      boolean overlaps = false;
      Optional<ResourceVF> oResourceVF = storage.readVF(ResourceVF.class, resourceUUID);
      if (oResourceVF.isPresent()) {
        FramedGraphTraversal<ResourceVF, Vertex> traversal = oResourceVF.get().start()//
            .in(TextRangeAnnotationVF.HAS_RESOURCE)//
            .has("name", annotation.getName())//
            .has("annotatorCode", annotation.getAnnotator());
        Integer start1 = annotation.getPosition().getOffset();
        Integer end1 = start1 + annotation.getPosition().getLength();
        Predicate<Vertex> overlapsWithAnnotation = t -> {
          Integer start2 = (Integer) t.property("offset").value();
          Integer end2 = start2 + (Integer) t.property("length").value();
          return start1 <= end2 && start2 <= end1;
        };
        overlaps = StreamUtil.parallelStream(traversal)//
            .filter(overlapsWithAnnotation)//
            .findAny()//
            .isPresent();
      }
      return overlaps;
    });
  }

  @Override
  public AlexandriaAnnotation deprecateAnnotation(UUID annotationId, AlexandriaAnnotation updatedAnnotation) {
    AnnotationVF annotationVF = storage.runInTransaction(() -> deprecateAnnotationVF(annotationId, updatedAnnotation));
    return deframeAnnotation(annotationVF);
  }

  private AnnotationVF deprecateAnnotationVF(UUID annotationId, AlexandriaAnnotation updatedAnnotation) {
    // check if there's an annotation with the given id
    AnnotationVF oldAnnotationVF = storage.readVF(AnnotationVF.class, annotationId)//
        .orElseThrow(annotationNotFound(annotationId));
    if (oldAnnotationVF.isTentative()) {
      throw incorrectStateException(annotationId, "tentative");
    } else if (oldAnnotationVF.isDeleted()) {
      throwBadRequest(annotationId, "deleted");
    } else if (oldAnnotationVF.isDeprecated()) {
      throwBadRequest(annotationId, "already deprecated");
    }

    AlexandriaAnnotationBody newBody = updatedAnnotation.getBody();
    Optional<AlexandriaAnnotationBody> optionalBody = findAnnotationBodyWithTypeAndValue(newBody.getType(), newBody.getValue());
    AlexandriaAnnotationBody body;
    if (optionalBody.isPresent()) {
      body = optionalBody.get();
    } else {
      AnnotationBodyVF annotationBodyVF = frameAnnotationBody(newBody);
      updateState(annotationBodyVF, AlexandriaState.CONFIRMED);
      body = newBody;
    }

    AlexandriaProvenance tmpProvenance = updatedAnnotation.getProvenance();
    TentativeAlexandriaProvenance provenance = new TentativeAlexandriaProvenance(tmpProvenance.getWho(), tmpProvenance.getWhen(), tmpProvenance.getWhy());
    AlexandriaAnnotation newAnnotation = new AlexandriaAnnotation(updatedAnnotation.getId(), body, provenance);
    AnnotationVF newAnnotationVF = frameAnnotation(newAnnotation);

    AnnotationVF annotatedAnnotation = oldAnnotationVF.getAnnotatedAnnotation();
    if (annotatedAnnotation != null) {
      newAnnotationVF.setAnnotatedAnnotation(annotatedAnnotation);
    } else {
      ResourceVF annotatedResource = oldAnnotationVF.getAnnotatedResource();
      newAnnotationVF.setAnnotatedResource(annotatedResource);
    }
    newAnnotationVF.setDeprecatedAnnotation(oldAnnotationVF);
    newAnnotationVF.setRevision(oldAnnotationVF.getRevision() + 1);
    updateState(newAnnotationVF, AlexandriaState.CONFIRMED);

    oldAnnotationVF.setAnnotatedAnnotation(null);
    oldAnnotationVF.setAnnotatedResource(null);
    oldAnnotationVF.setUuid(oldAnnotationVF.getUuid() + "." + oldAnnotationVF.getRevision());
    updateState(oldAnnotationVF, AlexandriaState.DEPRECATED);
    return newAnnotationVF;
  }

  private void throwBadRequest(UUID annotationId, String string) {
    throw new BadRequestException("annotation " + annotationId + " is " + string);
  }

  @Override
  public void confirmResource(UUID uuid) {
    storage.runInTransaction(() -> {
      ResourceVF resourceVF = storage.readVF(ResourceVF.class, uuid)//
          .orElseThrow(resourceNotFound(uuid));
      updateState(resourceVF, AlexandriaState.CONFIRMED);
    });
  }

  @Override
  public void confirmAnnotation(UUID uuid) {
    storage.runInTransaction(() -> {
      AnnotationVF annotationVF = storage.readVF(AnnotationVF.class, uuid).orElseThrow(annotationNotFound(uuid));
      updateState(annotationVF, AlexandriaState.CONFIRMED);
      updateState(annotationVF.getBody(), AlexandriaState.CONFIRMED);
      AnnotationVF deprecatedAnnotation = annotationVF.getDeprecatedAnnotation();
      if (deprecatedAnnotation != null && !deprecatedAnnotation.isDeprecated()) {
        updateState(deprecatedAnnotation, AlexandriaState.DEPRECATED);
      }
    });
  }

  @Override
  public void deleteAnnotation(AlexandriaAnnotation annotation) {
    storage.runInTransaction(() -> {
      UUID uuid = annotation.getId();
      AnnotationVF annotationVF = storage.readVF(AnnotationVF.class, uuid).get();
      if (annotation.isTentative()) {
        // remove from database

        AnnotationBodyVF body = annotationVF.getBody();
        List<AnnotationVF> ofAnnotations = body.getOfAnnotationList();
        if (ofAnnotations.size() == 1) {
          String annotationBodyId = body.getUuid();
          storage.removeVertexWithId(annotationBodyId);
        }

        // remove has_body edge
        annotationVF.setBody(null);

        // remove annotates edge
        annotationVF.setAnnotatedAnnotation(null);
        annotationVF.setAnnotatedResource(null);

        String annotationId = uuid.toString();
        storage.removeVertexWithId(annotationId);

      } else {
        // set state
        updateState(annotationVF, AlexandriaState.DELETED);
      }
    });
  }

  @Override
  public AlexandriaAnnotationBody createAnnotationBody(UUID uuid, String type, String value, TentativeAlexandriaProvenance provenance) {
    AlexandriaAnnotationBody body = new AlexandriaAnnotationBody(uuid, type, value, provenance);
    storeAnnotationBody(body);
    return body;
  }

  @Override
  public Optional<AlexandriaAnnotationBody> readAnnotationBody(UUID uuid) {
    throw new NotImplementedException();
  }

  @Override
  public void setTextView(UUID resourceUUID, String viewId, TextView textView, TextViewDefinition textViewDefinition) {
    storage.runInTransaction(() -> {
      ResourceVF resourceVF = storage.readVF(ResourceVF.class, resourceUUID).get();
      String json;
      try {
        String serializedTextViewMap = resourceVF.getSerializedTextViewMap();
        Map<String, TextView> textViewMap = deserializeToTextViewMap(serializedTextViewMap);
        textViewMap.put(viewId, textView);
        json = serializeTextViewMap(textViewMap);
        resourceVF.setSerializedTextViewMap(json);

        String serializedTextViewDefinitionMap = resourceVF.getSerializedTextViewDefinitionMap();
        Map<String, TextViewDefinition> textViewDefinitionMap = deserializeToTextViewDefinitionMap(serializedTextViewDefinitionMap);
        textViewDefinitionMap.put(viewId, textViewDefinition);
        json = serializeTextViewDefinitionMap(textViewDefinitionMap);
        resourceVF.setSerializedTextViewDefinitionMap(json);

      } catch (Exception e) {
        e.printStackTrace();
        throw new RuntimeException(e);
      }
    });
  }

  @Override
  public List<TextView> getTextViewsForResource(UUID resourceUUID) {
    List<TextView> textViews = new ArrayList<>();
    Set<String> viewNames = Sets.newHashSet();

    return storage.runInTransaction(() -> {
      ResourceVF resourceVF = storage.readVF(ResourceVF.class, resourceUUID).get();
      while (resourceVF != null) {
        String serializedTextViews = resourceVF.getSerializedTextViewMap();
        UUID uuid = UUID.fromString(resourceVF.getUuid());
        try {
          deserializeToTextViews(serializedTextViews).stream().filter(v -> !viewNames.contains(v.getName())).forEach((tv) -> {
            tv.setTextViewDefiningResourceId(uuid);
            textViews.add(tv);
            viewNames.add(tv.getName());
          });

        } catch (Exception e) {
          e.printStackTrace();
          throw new RuntimeException(e);
        }
        resourceVF = resourceVF.getParentResource();
      }
      return textViews;
    });
  }

  @Override
  public Optional<TextView> getTextView(UUID resourceId, String view) {
    TextView textView = storage.runInTransaction(() -> {
      ResourceVF resourceVF = storage.readVF(ResourceVF.class, resourceId).get();
      String serializedTextViews = resourceVF.getSerializedTextViewMap();
      try {
        Map<String, TextView> textViewMap = deserializeToTextViewMap(serializedTextViews);
        List<TextView> textViews = textViewMap//
            .entrySet()//
            .stream()//
            .filter(e -> e.getKey().equals(view))//
            .map(this::setName)//
            .collect(toList());
        return textViews.isEmpty() ? null : textViews.get(0);
      } catch (Exception e) {
        e.printStackTrace();
        throw new RuntimeException(e);
      }
    });
    return Optional.ofNullable(textView);
  }

  @Override
  public Optional<TextViewDefinition> getTextViewDefinition(UUID resourceId, String view) {
    TextViewDefinition textViewDefinition = storage.runInTransaction(() -> {
      ResourceVF resourceVF = storage.readVF(ResourceVF.class, resourceId).get();
      String serializedTextViewDefinitions = resourceVF.getSerializedTextViewDefinitionMap();
      try {
        Map<String, TextViewDefinition> textViewDefinitionMap = deserializeToTextViewDefinitionMap(serializedTextViewDefinitions);
        return textViewDefinitionMap.get(view);

      } catch (Exception e) {
        e.printStackTrace();
        throw new RuntimeException(e);
      }
    });
    return Optional.ofNullable(textViewDefinition);
  }

  @Override
  public SearchResult execute(AlexandriaQuery query) {
    return storage.runInTransaction(() -> {
      Stopwatch stopwatch = Stopwatch.createStarted();
      List<Map<String, Object>> processQuery = processQuery(query);
      stopwatch.stop();
      long elapsedMillis = stopwatch.elapsed(TimeUnit.MILLISECONDS);

      return new SearchResult(locationBuilder)//
          .setId(UUID.randomUUID())//
          .setQuery(query)//
          .setSearchDurationInMilliseconds(elapsedMillis)//
          .setResults(processQuery);
    });
  }

  @Override
  public Map<String, Object> getMetadata() {
    return storage.runInTransaction(() -> {
      Map<String, Object> metadata = Maps.newLinkedHashMap();
      metadata.put("type", this.getClass().getCanonicalName());
      metadata.put("storage", storage.getMetadata());
      return metadata;
    });
  }

  @Override
  public void destroy() {
    // Log.info("destroy called");
    storage.destroy();
    // Log.info("destroy done");
  }

  @Override
  public void exportDb(String format, String filename) {
    storage.runInTransaction(Unchecked.runnable(() -> storage.writeGraph(DumpFormat.valueOf(format), filename)));
  }

  @Override
  public void importDb(String format, String filename) {
    storage = clearGraph();
    storage.runInTransaction(Unchecked.runnable(() -> storage.readGraph(DumpFormat.valueOf(format), filename)));
  }

  @Override
  public void runInTransaction(Runnable runner) {
    storage.runInTransaction(runner);
  }

  @Override
  public boolean storeTextGraph(UUID resourceId, ParseResult result) {
    if (readResource(resourceId).isPresent()) {
      textGraphService.storeTextGraph(resourceId, result);
      return true;
    }
    // something went wrong
    readResource(resourceId).get().setHasText(false);
    return false;
  }

  @Override
  public Stream<TextGraphSegment> getTextGraphSegmentStream(UUID resourceId) {
    return textGraphService.getTextGraphSegmentStream(resourceId);
  }

  @Override
  public Stream<TextAnnotation> getTextAnnotationStream(UUID resourceId) {
    return textGraphService.getTextAnnotationStream(resourceId);
  }

  @Override
  public void updateTextAnnotation(TextAnnotation textAnnotation) {
    textGraphService.updateTextAnnotation(textAnnotation);
  }

  @Override
  public void wrapContentInChildTextAnnotation(TextAnnotation existingTextAnnotation, TextAnnotation newTextAnnotation) {
    textGraphService.wrapContentInChildTextAnnotation(existingTextAnnotation, newTextAnnotation);
  }

  // - other public methods -//

  public void createSubResource(AlexandriaResource subResource) {
    storage.runInTransaction(() -> {
      final ResourceVF rvf;
      final UUID uuid = subResource.getId();
      if (storage.existsVF(ResourceVF.class, uuid)) {
        rvf = storage.readVF(ResourceVF.class, uuid).get();
      } else {
        rvf = storage.createVF(ResourceVF.class);
        rvf.setUuid(uuid.toString());
      }

      rvf.setCargo(subResource.getCargo());
      final UUID parentId = UUID.fromString(subResource.getParentResourcePointer().get().getIdentifier());
      Optional<ResourceVF> parentVF = storage.readVF(ResourceVF.class, parentId);
      rvf.setParentResource(parentVF.get());

      setAlexandriaVFProperties(rvf, subResource);
    });
  }

  public void createOrUpdateAnnotation(AlexandriaAnnotation annotation) {
    storage.runInTransaction(() -> {
      final AnnotationVF avf;
      final UUID uuid = annotation.getId();
      if (storage.existsVF(AnnotationVF.class, uuid)) {
        avf = storage.readVF(AnnotationVF.class, uuid).get();
      } else {
        avf = storage.createVF(AnnotationVF.class);
        avf.setUuid(uuid.toString());
      }

      setAlexandriaVFProperties(avf, annotation);
    });
  }

  void annotateResourceWithAnnotation(AlexandriaResource resource, AlexandriaAnnotation newAnnotation) {
    storage.runInTransaction(() -> {
      AnnotationVF avf = frameAnnotation(newAnnotation);
      ResourceVF resourceToAnnotate = storage.readVF(ResourceVF.class, resource.getId()).get();
      avf.setAnnotatedResource(resourceToAnnotate);
    });
  }

  public void storeAnnotationBody(AlexandriaAnnotationBody body) {
    storage.runInTransaction(() -> frameAnnotationBody(body));
  }

  private void annotateAnnotationWithAnnotation(AlexandriaAnnotation annotation, AlexandriaAnnotation newAnnotation) {
    storage.runInTransaction(() -> {
      AnnotationVF avf = frameAnnotation(newAnnotation);
      UUID id = annotation.getId();
      AnnotationVF annotationToAnnotate = storage.readVF(AnnotationVF.class, id).get();
      avf.setAnnotatedAnnotation(annotationToAnnotate);
    });
  }

  public void dumpToGraphSON(OutputStream os) throws IOException {
    storage.runInTransaction(Unchecked.runnable(() -> storage.dumpToGraphSON(os)));
  }

  public void dumpToGraphML(OutputStream os) throws IOException {
    storage.runInTransaction(Unchecked.runnable(() -> storage.dumpToGraphML(os)));
  }

  // - package methods -//

  Storage clearGraph() {
    storage.getVertexTraversal()//
        .forEachRemaining(org.apache.tinkerpop.gremlin.structure.Element::remove);
    return storage;
  }

  void createOrUpdateResource(AlexandriaResource resource) {
    final UUID uuid = resource.getId();
    storage.runInTransaction(() -> {
      final ResourceVF rvf;
      if (storage.existsVF(ResourceVF.class, uuid)) {
        rvf = storage.readVF(ResourceVF.class, uuid).get();
      } else {
        rvf = storage.createVF(ResourceVF.class);
        rvf.setUuid(uuid.toString());
      }

      rvf.setCargo(resource.getCargo());

      setAlexandriaVFProperties(rvf, resource);
    });
  }

  void updateState(AlexandriaVF vf, AlexandriaState newState) {
    vf.setState(newState.name());
    vf.setStateSince(Instant.now().getEpochSecond());
  }

  // - private methods -//

  private String serializeTextViewMap(Map<String, TextView> textViewMap) throws JsonProcessingException {
    return TEXTVIEW_WRITER.writeValueAsString(textViewMap);
  }

  private Map<String, TextView> deserializeToTextViewMap(String json) throws IOException {
    if (StringUtils.isEmpty(json)) {
      return Maps.newHashMap();
    }
    Map<String, TextView> textViewMap = TEXTVIEW_READER.readValue(json);
    return textViewMap;
  }

  private String serializeTextViewDefinitionMap(Map<String, TextViewDefinition> textViewDefinitionMap) throws JsonProcessingException {
    return TEXTVIEWDEFINITION_WRITER.writeValueAsString(textViewDefinitionMap);
  }

  private Map<String, TextViewDefinition> deserializeToTextViewDefinitionMap(String json) throws JsonProcessingException, IOException {
    if (StringUtils.isEmpty(json)) {
      return Maps.newHashMap();
    }
    Map<String, TextViewDefinition> textViewDefinitionMap = TEXTVIEWDEFINITION_READER.readValue(json);
    return textViewDefinitionMap;
  }

  private AlexandriaAnnotation createAnnotation(AlexandriaAnnotationBody annotationbody, TentativeAlexandriaProvenance provenance) {
    return new AlexandriaAnnotation(UUID.randomUUID(), annotationbody, provenance);
  }

  private AlexandriaAnnotation createAnnotation(AlexandriaTextLocator textLocator, AlexandriaAnnotationBody annotationbody, TentativeAlexandriaProvenance provenance) {
    AlexandriaAnnotation alexandriaAnnotation = createAnnotation(annotationbody, provenance);
    alexandriaAnnotation.setLocator(textLocator);
    return alexandriaAnnotation;
  }

  private AlexandriaResource deframeResource(ResourceVF rvf) {
    TentativeAlexandriaProvenance provenance = deframeProvenance(rvf);
    UUID uuid = getUUID(rvf);
    AlexandriaResource resource = new AlexandriaResource(uuid, provenance);
    resource.setHasText(rvf.getHasText());
    resource.setCargo(rvf.getCargo());
    resource.setState(AlexandriaState.valueOf(rvf.getState()));
    resource.setStateSince(Instant.ofEpochSecond(rvf.getStateSince()));
    setTextViews(rvf, resource);

    for (AnnotationVF annotationVF : rvf.getAnnotatedBy()) {
      AlexandriaAnnotation annotation = deframeAnnotation(annotationVF);
      resource.addAnnotation(annotation);
    }
    ResourceVF parentResource = rvf.getParentResource();
    if (parentResource != null) {
      resource.setParentResourcePointer(new IdentifiablePointer<>(AlexandriaResource.class, parentResource.getUuid()));
      // ResourceVF ancestorResource = parentResource;
      // while (ancestorResource != null && StringUtils.isEmpty(ancestorResource.getSerializedTextViewMap())) {
      // ancestorResource = ancestorResource.getParentResource();
      // }
      // if (ancestorResource != null) {
      // resource.setFirstAncestorResourceWithBaseLayerDefinitionPointer(new IdentifiablePointer<>(AlexandriaResource.class, ancestorResource.getUuid()));
      // }
    }
    rvf.getSubResources().stream()//
        .forEach(vf -> resource.addSubResourcePointer(new IdentifiablePointer<>(AlexandriaResource.class, vf.getUuid())));
    return resource;
  }

  private AnnotatorVF frameAnnotator(Annotator annotator) {
    AnnotatorVF avf = storage.createVF(AnnotatorVF.class);
    avf.setCode(annotator.getCode());
    avf.setDescription(annotator.getDescription());
    return avf;
  }

  private Annotator deframeAnnotator(AnnotatorVF avf) {
    return new Annotator()//
        .setCode(avf.getCode())//
        .setDescription(avf.getDescription())//
        .setResourceURI(locationBuilder.locationOf(AlexandriaResource.class, avf.getResource().getUuid()));
  }

  private void updateTextRangeAnnotation(TextRangeAnnotationVF vf, TextRangeAnnotation annotation) {
    vf.setUuid(annotation.getId().toString());
    vf.setName(annotation.getName());
    vf.setAnnotatorCode(annotation.getAnnotator());
    Position position = annotation.getPosition();
    vf.setXmlId(position.getXmlId());
    vf.setOffset(position.getOffset());
    vf.setLength(position.getLength());
  }

  private TextRangeAnnotation deframeTextRangeAnnotation(TextRangeAnnotationVF vf) {
    Position position = new Position()//
        .setXmlId(vf.getXmlId())//
        .setOffset(vf.getOffset())//
        .setLength(vf.getLength());
    return new TextRangeAnnotation()//
        .setId(UUID.fromString(vf.getUuid()))//
        .setName(vf.getName())//
        .setAnnotator(vf.getAnnotatorCode())//
        .setPosition(position);
  }

  private void setTextViews(ResourceVF rvf, AlexandriaResource resource) {
    String textViewsJson = rvf.getSerializedTextViewMap();
    if (StringUtils.isNotEmpty(textViewsJson)) {
      try {
        List<TextView> textViews = deserializeToTextViews(textViewsJson);
        resource.setDirectTextViews(textViews);
      } catch (IOException e) {
        e.printStackTrace();
        throw new RuntimeException(e);
      }
    }
  }

  private List<TextView> deserializeToTextViews(String textViewsJson) throws IOException {
    Map<String, TextView> textViewMap = deserializeToTextViewMap(textViewsJson);
    List<TextView> textViews = textViewMap.entrySet()//
        .stream()//
        .map(this::setName)//
        .collect(toList());
    return textViews;
  }

  private TextView setName(Entry<String, TextView> entry) {
    TextView textView = entry.getValue();
    textView.setName(entry.getKey());
    return textView;
  }

  AnnotationVF frameAnnotation(AlexandriaAnnotation newAnnotation) {
    AnnotationVF avf = storage.createVF(AnnotationVF.class);
    setAlexandriaVFProperties(avf, newAnnotation);
    avf.setRevision(newAnnotation.getRevision());
    if (newAnnotation.getLocator() != null) {
      avf.setLocator(newAnnotation.getLocator().toString());
    }
    UUID bodyId = newAnnotation.getBody().getId();
    AnnotationBodyVF bodyVF = storage.readVF(AnnotationBodyVF.class, bodyId).get();
    avf.setBody(bodyVF);
    return avf;
  }

  private AlexandriaAnnotation deframeAnnotation(AnnotationVF annotationVF) {
    TentativeAlexandriaProvenance provenance = deframeProvenance(annotationVF);
    UUID uuid = getUUID(annotationVF);
    AlexandriaAnnotationBody body = deframeAnnotationBody(annotationVF.getBody());
    AlexandriaAnnotation annotation = new AlexandriaAnnotation(uuid, body, provenance);
    if (annotationVF.getLocator() != null) {
      try {
        annotation.setLocator(new TextLocatorFactory(this).fromString(annotationVF.getLocator()));
      } catch (TextLocatorParseException e) {
        e.printStackTrace();
      }
    }
    annotation.setState(AlexandriaState.valueOf(annotationVF.getState()));
    annotation.setStateSince(Instant.ofEpochSecond(annotationVF.getStateSince()));
    if (annotationVF.getRevision() == null) { // update old data
      annotationVF.setRevision(0);
    }
    annotation.setRevision(annotationVF.getRevision());

    AnnotationVF annotatedAnnotation = annotationVF.getAnnotatedAnnotation();
    if (annotatedAnnotation == null) {
      ResourceVF annotatedResource = annotationVF.getAnnotatedResource();
      if (annotatedResource != null) {
        annotation.setAnnotatablePointer(new IdentifiablePointer<>(AlexandriaResource.class, annotatedResource.getUuid()));
      }
    } else {
      annotation.setAnnotatablePointer(new IdentifiablePointer<>(AlexandriaAnnotation.class, annotatedAnnotation.getUuid()));
    }
    for (AnnotationVF avf : annotationVF.getAnnotatedBy()) {
      AlexandriaAnnotation annotationAnnotation = deframeAnnotation(avf);
      annotation.addAnnotation(annotationAnnotation);
    }
    return annotation;
  }

  private AnnotationBodyVF frameAnnotationBody(AlexandriaAnnotationBody body) {
    AnnotationBodyVF abvf = storage.createVF(AnnotationBodyVF.class);
    setAlexandriaVFProperties(abvf, body);
    abvf.setType(body.getType());
    abvf.setValue(body.getValue());
    return abvf;
  }

  private AlexandriaAnnotationBody deframeAnnotationBody(AnnotationBodyVF annotationBodyVF) {
    TentativeAlexandriaProvenance provenance = deframeProvenance(annotationBodyVF);
    UUID uuid = getUUID(annotationBodyVF);
    return new AlexandriaAnnotationBody(uuid, annotationBodyVF.getType(), annotationBodyVF.getValue(), provenance);
  }

  private TentativeAlexandriaProvenance deframeProvenance(AlexandriaVF avf) {
    String provenanceWhen = avf.getProvenanceWhen();
    return new TentativeAlexandriaProvenance(avf.getProvenanceWho(), Instant.parse(provenanceWhen), avf.getProvenanceWhy());
  }

  private void setAlexandriaVFProperties(AlexandriaVF vf, Accountable accountable) {
    vf.setUuid(accountable.getId().toString());

    vf.setState(accountable.getState().toString());
    vf.setStateSince(accountable.getStateSince().getEpochSecond());

    AlexandriaProvenance provenance = accountable.getProvenance();
    vf.setProvenanceWhen(provenance.getWhen().toString());
    vf.setProvenanceWho(provenance.getWho());
    vf.setProvenanceWhy(provenance.getWhy());
  }

  // framedGraph methods

  private Supplier<NotFoundException> annotationNotFound(UUID id) {
    return () -> new NotFoundException("no annotation found with uuid " + id);
  }

  private Supplier<NotFoundException> resourceNotFound(UUID id) {
    return () -> new NotFoundException("no resource found with uuid " + id);
  }

  private BadRequestException incorrectStateException(UUID oldAnnotationId, String string) {
    return new BadRequestException("annotation " + oldAnnotationId + " is " + string);
  }

  private UUID getUUID(AlexandriaVF vf) {
    return UUID.fromString(vf.getUuid().replaceFirst("\\..$", "")); // remove revision suffix for deprecated annotations
  }

  private List<Map<String, Object>> processQuery(AlexandriaQuery query) {
    ParsedAlexandriaQuery pQuery = alexandriaQueryParser.parse(query);

    Predicate<AnnotationVF> predicate = pQuery.getPredicate();
    Comparator<AnnotationVF> comparator = pQuery.getResultComparator();
    Function<AnnotationVF, Map<String, Object>> mapper = pQuery.getResultMapper();

    Stream<AnnotationVF> stream = pQuery.getAnnotationVFFinder().apply(storage);

    Stream<Map<String, Object>> mapStream = stream//
        .filter(predicate)//
        .sorted(comparator)//
        .map(mapper);
    if (pQuery.isDistinct()) {
      mapStream = mapStream.distinct();
    }
    if (pQuery.doGrouping()) {
      Stream<Map<String, Object>> groupByStream = mapStream//
          .collect(groupingBy(pQuery::concatenateGroupByFieldsValues))//
          .values().stream()//
          .map(pQuery::collectListFieldValues);
      mapStream = groupByStream;
    }
    List<Map<String, Object>> results = mapStream//
        .collect(toList());
    return results;
  }

  private ResourceVF readExisitingResourceVF(UUID uuid) {
    ResourceVF resourcevf = storage.runInTransaction(() -> storage.readVF(ResourceVF.class, uuid))//
        .orElseThrow(() -> new NotFoundException("no resource found with uuid " + uuid));
    return resourcevf;
  }

}
