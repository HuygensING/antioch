package nl.knaw.huygens.antioch.service;

/*
 * #%L
 * antioch-service
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

import com.google.common.base.Stopwatch;
import com.google.common.collect.Maps;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import nl.knaw.huygens.antioch.api.model.AntiochState;
import nl.knaw.huygens.antioch.api.model.search.AntiochQuery;
import nl.knaw.huygens.antioch.endpoint.LocationBuilder;
import nl.knaw.huygens.antioch.endpoint.search.SearchResult;
import nl.knaw.huygens.antioch.exception.BadRequestException;
import nl.knaw.huygens.antioch.exception.NotFoundException;
import nl.knaw.huygens.antioch.model.*;
import nl.knaw.huygens.antioch.query.AntiochQueryParser;
import nl.knaw.huygens.antioch.query.ParsedAntiochQuery;
import nl.knaw.huygens.antioch.storage.DumpFormat;
import nl.knaw.huygens.antioch.storage.Storage;
import nl.knaw.huygens.antioch.storage.frames.*;
import nl.knaw.huygens.antioch.textlocator.AntiochTextLocator;
import nl.knaw.huygens.antioch.textlocator.TextLocatorFactory;
import nl.knaw.huygens.antioch.textlocator.TextLocatorParseException;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.jooq.lambda.Unchecked;
import peapod.FramedGraphTraversal;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.io.OutputStream;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.TemporalAmount;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Stream;

@Singleton
public class TinkerPopService implements AntiochService {
  private static final TemporalAmount TENTATIVES_TTL = Duration.ofDays(1);

  private Storage storage;
  private final LocationBuilder locationBuilder;
  private final AntiochQueryParser antiochQueryParser;

  @Inject
  public TinkerPopService(Storage storage, LocationBuilder locationBuilder) {
    // Log.trace("{} created, locationBuilder=[{}]", getClass().getSimpleName(), locationBuilder);
    this.locationBuilder = locationBuilder;
    this.antiochQueryParser = new AntiochQueryParser(locationBuilder);
    setStorage(storage);
  }

  public void setStorage(Storage storage) {
    this.storage = storage;
    // this.textGraphService = new TextGraphService(storage);
  }

  // - AntiochService methods -//
  // use storage.runInTransaction for transactions

  @Override
  public boolean createOrUpdateResource(UUID uuid, String ref, TentativeAntiochProvenance provenance, AntiochState state) {
    return storage.runInTransaction(() -> {
      AntiochResource resource;
      boolean result;

      if (storage.existsVF(ResourceVF.class, uuid)) {
        resource = getOptionalResource(uuid).get();
        result = false;

      } else {
        resource = new AntiochResource(uuid, provenance);
        result = true;
      }
      resource.setCargo(ref);
      resource.setState(state);
      createOrUpdateResource(resource);
      return result;
    });
  }

  @Override
  public AntiochResource createResource(UUID resourceUUID, String ref, TentativeAntiochProvenance provenance, AntiochState state) {
    return storage.runInTransaction(() -> {
      AntiochResource resource = new AntiochResource(resourceUUID, provenance);
      resource.setCargo(ref);
      resource.setState(state);
      createOrUpdateResource(resource);
      return resource;
    });
  }

  private Optional<AntiochResource> getOptionalResource(UUID uuid) {
    return storage.readVF(ResourceVF.class, uuid).map(this::deframeResource);
  }

  private Optional<AntiochResource> getOptionalResourceWithUniqueRef(String ref) {
    FramedGraphTraversal<Object, ResourceVF> traversal = storage.find(ResourceVF.class).has(ResourceVF.Properties.CARGO, ref);
    AntiochResource antiochResource = traversal.hasNext() ? deframeResourceLite(traversal.next()) : null;
    return Optional.ofNullable(antiochResource);
  }

  @Override
  public AntiochAnnotation annotate(AntiochResource resource, AntiochAnnotationBody annotationbody, TentativeAntiochProvenance provenance) {
    AntiochAnnotation newAnnotation = createAnnotation(annotationbody, provenance);
    annotateResourceWithAnnotation(resource, newAnnotation);
    return newAnnotation;
  }

  @Override
  public AntiochAnnotation annotate(AntiochResource resource, AntiochTextLocator textLocator, AntiochAnnotationBody annotationbody, TentativeAntiochProvenance provenance) {
    AntiochAnnotation newAnnotation = createAnnotation(textLocator, annotationbody, provenance);
    annotateResourceWithAnnotation(resource, newAnnotation);
    return newAnnotation;
  }

  @Override
  public AntiochAnnotation annotate(AntiochAnnotation annotation, AntiochAnnotationBody annotationbody, TentativeAntiochProvenance provenance) {
    AntiochAnnotation newAnnotation = createAnnotation(annotationbody, provenance);
    annotateAnnotationWithAnnotation(annotation, newAnnotation);
    return newAnnotation;
  }

  @Override
  public AntiochResource createSubResource(UUID uuid, UUID parentUuid, String sub, TentativeAntiochProvenance provenance) {
    AntiochResource subresource = new AntiochResource(uuid, provenance);
    subresource.setCargo(sub);
    subresource.setParentResourcePointer(new IdentifiablePointer<>(AntiochResource.class, parentUuid.toString()));
    createSubResource(subresource);
    return subresource;
  }

  @Override
  public Optional<? extends Accountable> dereference(IdentifiablePointer<? extends Accountable> pointer) {
    Class<? extends Accountable> aClass = pointer.getIdentifiableClass();
    UUID uuid = UUID.fromString(pointer.getIdentifier());
    if (AntiochResource.class.equals(aClass)) {
      return readResource(uuid);

    } else if (AntiochAnnotation.class.equals(aClass)) {
      return readAnnotation(uuid);

    } else {
      throw new RuntimeException("unexpected accountableClass: " + aClass.getName());
    }
  }

  @Override
  public Optional<AntiochResource> readResource(UUID uuid) {
    return storage.runInTransaction(() -> getOptionalResource(uuid));
  }

  @Override
  public Optional<AntiochResource> readResourceWithUniqueRef(String resourceRef) {
    return storage.runInTransaction(() -> getOptionalResourceWithUniqueRef(resourceRef));
  }

  @Override
  public Optional<AntiochAnnotation> readAnnotation(UUID uuid) {
    return storage.runInTransaction(() -> storage.readVF(AnnotationVF.class, uuid).map(this::deframeAnnotation));
  }

  @Override
  public Optional<AntiochAnnotation> readAnnotation(UUID uuid, Integer revision) {
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
  public Optional<AntiochAnnotationBody> findAnnotationBodyWithTypeAndValue(String type, String value) {
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
  public Optional<AntiochResource> findSubresourceWithSubAndParentId(String sub, UUID parentId) {
    return storage.runInTransaction(//
        () -> storage.getResourceVertexTraversal()//
            .has(Storage.IDENTIFIER_PROPERTY, parentId.toString())//
            .in(ResourceVF.EdgeLabels.PART_OF)//
            .has(ResourceVF.Properties.CARGO, sub)//
            .toList()//
            .stream()//
            .map(this::deframeResource)//
            .findAny()//
    );
  }

  @Override
  public List<AntiochResource> readSubResources(UUID uuid) {
    ResourceVF resourcevf = readExistingResourceVF(uuid);
    return resourcevf.getSubResources().stream()//
        .map(this::deframeResource)//
        .sorted()//
        .collect(toList());
  }


  @Override
  public AntiochAnnotation deprecateAnnotation(UUID annotationId, AntiochAnnotation updatedAnnotation) {
    AnnotationVF annotationVF = storage.runInTransaction(() -> deprecateAnnotationVF(annotationId, updatedAnnotation));
    return deframeAnnotation(annotationVF);
  }

  private AnnotationVF deprecateAnnotationVF(UUID annotationId, AntiochAnnotation updatedAnnotation) {
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

    AntiochAnnotationBody newBody = updatedAnnotation.getBody();
    Optional<AntiochAnnotationBody> optionalBody = findAnnotationBodyWithTypeAndValue(newBody.getType(), newBody.getValue());
    AntiochAnnotationBody body;
    if (optionalBody.isPresent()) {
      body = optionalBody.get();
    } else {
      AnnotationBodyVF annotationBodyVF = frameAnnotationBody(newBody);
      updateState(annotationBodyVF, AntiochState.CONFIRMED);
      body = newBody;
    }

    // update the uuid of the (to be) deprecated annotation, so the annotationuuid can be used for the new annotation
    oldAnnotationVF.setUuid(oldAnnotationVF.getUuid() + "." + oldAnnotationVF.getRevision());

    AntiochProvenance tmpProvenance = updatedAnnotation.getProvenance();
    TentativeAntiochProvenance provenance = new TentativeAntiochProvenance(tmpProvenance.getWho(), tmpProvenance.getWhen(), tmpProvenance.getWhy());
    AntiochAnnotation newAnnotation = new AntiochAnnotation(updatedAnnotation.getId(), body, provenance);
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
    updateState(newAnnotationVF, AntiochState.CONFIRMED);

    oldAnnotationVF.setAnnotatedAnnotation(null);
    oldAnnotationVF.setAnnotatedResource(null);
    updateState(oldAnnotationVF, AntiochState.DEPRECATED);
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
      updateState(resourceVF, AntiochState.CONFIRMED);
    });
  }

  @Override
  public void confirmAnnotation(UUID uuid) {
    storage.runInTransaction(() -> {
      AnnotationVF annotationVF = storage.readVF(AnnotationVF.class, uuid).orElseThrow(annotationNotFound(uuid));
      updateState(annotationVF, AntiochState.CONFIRMED);
      updateState(annotationVF.getBody(), AntiochState.CONFIRMED);
      AnnotationVF deprecatedAnnotation = annotationVF.getDeprecatedAnnotation();
      if (deprecatedAnnotation != null && !deprecatedAnnotation.isDeprecated()) {
        updateState(deprecatedAnnotation, AntiochState.DEPRECATED);
      }
    });
  }

  @Override
  public void deleteAnnotation(AntiochAnnotation annotation) {
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
        updateState(annotationVF, AntiochState.DELETED);
      }
    });
  }

  @Override
  public AntiochAnnotationBody createAnnotationBody(UUID uuid, String type, String value, TentativeAntiochProvenance provenance) {
    AntiochAnnotationBody body = new AntiochAnnotationBody(uuid, type, value, provenance);
    storeAnnotationBody(body);
    return body;
  }

  @Override
  public Optional<AntiochAnnotationBody> readAnnotationBody(UUID uuid) {
    throw new NotImplementedException("readAnnotationBody");
  }


  @Override
  public SearchResult execute(AntiochQuery query) {
    return storage.runInTransaction(() -> {
      Stopwatch stopwatch = Stopwatch.createStarted();
      List<Map<String, Object>> results = processQuery(query);
      stopwatch.stop();
      long elapsedMillis = stopwatch.elapsed(TimeUnit.MILLISECONDS);

      return new SearchResult(locationBuilder)//
          .setId(UUID.randomUUID())//
          .setQuery(query)//
          .setSearchDurationInMilliseconds(elapsedMillis)//
          .setResults(results);
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
  public <A> A runInTransaction(Supplier<A> supplier) {
    return storage.runInTransaction(supplier);
  }

  // - other public methods -//

  public void createSubResource(AntiochResource subResource) {
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

      setAntiochVFProperties(rvf, subResource);
    });
  }

  public void createOrUpdateAnnotation(AntiochAnnotation annotation) {
    storage.runInTransaction(() -> {
      final AnnotationVF avf;
      final UUID uuid = annotation.getId();
      if (storage.existsVF(AnnotationVF.class, uuid)) {
        avf = storage.readVF(AnnotationVF.class, uuid).get();
      } else {
        avf = storage.createVF(AnnotationVF.class);
        avf.setUuid(uuid.toString());
      }

      setAntiochVFProperties(avf, annotation);
    });
  }

  void annotateResourceWithAnnotation(AntiochResource resource, AntiochAnnotation newAnnotation) {
    storage.runInTransaction(() -> {
      AnnotationVF avf = frameAnnotation(newAnnotation);
      ResourceVF resourceToAnnotate = storage.readVF(ResourceVF.class, resource.getId()).get();
      avf.setAnnotatedResource(resourceToAnnotate);
    });
  }

  public void storeAnnotationBody(AntiochAnnotationBody body) {
    storage.runInTransaction(() -> frameAnnotationBody(body));
  }

  private void annotateAnnotationWithAnnotation(AntiochAnnotation annotation, AntiochAnnotation newAnnotation) {
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
    storage.runInTransaction(() -> storage.getVertexTraversal()//
        .forEachRemaining(org.apache.tinkerpop.gremlin.structure.Element::remove));
    return storage;
  }

  void createOrUpdateResource(AntiochResource resource) {
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

      setAntiochVFProperties(rvf, resource);
    });
  }

  private void updateState(AntiochVF vf, AntiochState newState) {
    vf.setState(newState.name());
    vf.setStateSince(Instant.now().getEpochSecond());
  }

  // - private methods -//


  private AntiochAnnotation createAnnotation(AntiochAnnotationBody annotationbody, TentativeAntiochProvenance provenance) {
    return new AntiochAnnotation(UUID.randomUUID(), annotationbody, provenance);
  }

  private AntiochAnnotation createAnnotation(AntiochTextLocator textLocator, AntiochAnnotationBody annotationbody, TentativeAntiochProvenance provenance) {
    AntiochAnnotation antiochAnnotation = createAnnotation(annotationbody, provenance);
    antiochAnnotation.setLocator(textLocator);
    return antiochAnnotation;
  }

  private AntiochResource deframeResource(Vertex v) {
    ResourceVF rvf = storage.frameVertex(v, ResourceVF.class);
    return deframeResource(rvf);
  }

  private AntiochResource deframeResource(ResourceVF rvf) {
    AntiochResource resource = deframeResourceLite(rvf);
    // setTextViews(rvf, resource);

    for (AnnotationVF annotationVF : rvf.getAnnotatedBy()) {
      AntiochAnnotation annotation = deframeAnnotation(annotationVF);
      resource.addAnnotation(annotation);
    }
    ResourceVF parentResource = rvf.getParentResource();
    if (parentResource != null) {
      resource.setParentResourcePointer(new IdentifiablePointer<>(AntiochResource.class, parentResource.getUuid()));
      // ResourceVF ancestorResource = parentResource;
      // while (ancestorResource != null && StringUtils.isEmpty(ancestorResource.getSerializedTextViewMap())) {
      // ancestorResource = ancestorResource.getParentResource();
      // }
      // if (ancestorResource != null) {
      // resource.setFirstAncestorResourceWithBaseLayerDefinitionPointer(new IdentifiablePointer<>(AntiochResource.class, ancestorResource.getUuid()));
      // }
    }
    rvf.getSubResources()//
        .forEach(vf -> resource.addSubResourcePointer(new IdentifiablePointer<>(AntiochResource.class, vf.getUuid())));
    return resource;
  }

  private AntiochResource deframeResourceLite(ResourceVF rvf) {
    TentativeAntiochProvenance provenance = deframeProvenance(rvf);
    UUID uuid = getUUID(rvf);
    AntiochResource resource = new AntiochResource(uuid, provenance);
    resource.setHasText(rvf.getHasText());
    resource.setCargo(rvf.getCargo());
    resource.setState(AntiochState.valueOf(rvf.getState()));
    resource.setStateSince(Instant.ofEpochSecond(rvf.getStateSince()));
    return resource;
  }


  private AnnotationVF frameAnnotation(AntiochAnnotation newAnnotation) {
    AnnotationVF avf = storage.createVF(AnnotationVF.class);
    setAntiochVFProperties(avf, newAnnotation);
    avf.setRevision(newAnnotation.getRevision());
    if (newAnnotation.getLocator() != null) {
      avf.setLocator(newAnnotation.getLocator().toString());
    }
    UUID bodyId = newAnnotation.getBody().getId();
    AnnotationBodyVF bodyVF = storage.readVF(AnnotationBodyVF.class, bodyId).get();
    avf.setBody(bodyVF);
    return avf;
  }

  private AntiochAnnotation deframeAnnotation(AnnotationVF annotationVF) {
    TentativeAntiochProvenance provenance = deframeProvenance(annotationVF);
    UUID uuid = getUUID(annotationVF);
    AntiochAnnotationBody body = deframeAnnotationBody(annotationVF.getBody());
    AntiochAnnotation annotation = new AntiochAnnotation(uuid, body, provenance);
    if (annotationVF.getLocator() != null) {
      try {
        annotation.setLocator(new TextLocatorFactory(this).fromString(annotationVF.getLocator()));
      } catch (TextLocatorParseException e) {
        e.printStackTrace();
      }
    }
    annotation.setState(AntiochState.valueOf(annotationVF.getState()));
    annotation.setStateSince(Instant.ofEpochSecond(annotationVF.getStateSince()));
    if (annotationVF.getRevision() == null) { // update old data
      annotationVF.setRevision(0);
    }
    annotation.setRevision(annotationVF.getRevision());

    AnnotationVF annotatedAnnotation = annotationVF.getAnnotatedAnnotation();
    if (annotatedAnnotation == null) {
      ResourceVF annotatedResource = annotationVF.getAnnotatedResource();
      if (annotatedResource != null) {
        annotation.setAnnotatablePointer(new IdentifiablePointer<>(AntiochResource.class, annotatedResource.getUuid()));
      }
    } else {
      annotation.setAnnotatablePointer(new IdentifiablePointer<>(AntiochAnnotation.class, annotatedAnnotation.getUuid()));
    }
    for (AnnotationVF avf : annotationVF.getAnnotatedBy()) {
      AntiochAnnotation annotationAnnotation = deframeAnnotation(avf);
      annotation.addAnnotation(annotationAnnotation);
    }
    return annotation;
  }

  private AnnotationBodyVF frameAnnotationBody(AntiochAnnotationBody body) {
    AnnotationBodyVF abvf = storage.createVF(AnnotationBodyVF.class);
    setAntiochVFProperties(abvf, body);
    abvf.setType(body.getType());
    abvf.setValue(body.getValue());
    return abvf;
  }

  private AntiochAnnotationBody deframeAnnotationBody(AnnotationBodyVF annotationBodyVF) {
    TentativeAntiochProvenance provenance = deframeProvenance(annotationBodyVF);
    UUID uuid = getUUID(annotationBodyVF);
    return new AntiochAnnotationBody(uuid, annotationBodyVF.getType(), annotationBodyVF.getValue(), provenance);
  }

  private TentativeAntiochProvenance deframeProvenance(AntiochVF avf) {
    String provenanceWhen = avf.getProvenanceWhen();
    return new TentativeAntiochProvenance(avf.getProvenanceWho(), Instant.parse(provenanceWhen), avf.getProvenanceWhy());
  }

  private void setAntiochVFProperties(AntiochVF vf, Accountable accountable) {
    vf.setUuid(accountable.getId().toString());

    vf.setState(accountable.getState().toString());
    vf.setStateSince(accountable.getStateSince().getEpochSecond());

    AntiochProvenance provenance = accountable.getProvenance();
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

  protected UUID getUUID(IdentifiableVF vf) {
    return UUID.fromString(vf.getUuid().replaceFirst("\\..$", "")); // remove revision suffix for deprecated annotations
  }

  private List<Map<String, Object>> processQuery(AntiochQuery query) {
    ParsedAntiochQuery pQuery = antiochQueryParser.parse(query);

    Stream<Map<String, Object>> mapStream = pQuery.getResultStreamMapper().apply(storage);

    if (pQuery.isDistinct()) {
      mapStream = mapStream.distinct();
    }
    if (pQuery.doGrouping()) {
      mapStream = mapStream//
          .collect(groupingBy(pQuery::concatenateGroupByFieldsValues, LinkedHashMap::new, toList()))//
          .values().stream()//
          .map(pQuery::collectListFieldValues)//
          .map(this::addListSize);
      if (pQuery.sortOnListSize()) {
        mapStream = mapStream.sorted(pQuery.getListSizeComparator());
      }
    }
    return mapStream//
        .collect(toList());
  }

  private Map<String, Object> addListSize(Map<String, Object> resultMap) {
    resultMap.put(AntiochQueryParser.LIST_SIZE, ((List<Object>) resultMap.get("_list")).size());
    return resultMap;
  }

  protected ResourceVF readExistingResourceVF(UUID uuid) {
    return storage.runInTransaction(() -> storage.readVF(ResourceVF.class, uuid))//
        .orElseThrow(() -> new NotFoundException("no resource found with uuid " + uuid));
  }

  public Storage storage() {
    return storage;
  }

}
