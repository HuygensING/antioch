package nl.knaw.huygens.alexandria.service;

/*
 * #%L
 * alexandria-service
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
import nl.knaw.huygens.alexandria.api.model.AlexandriaState;
import nl.knaw.huygens.alexandria.api.model.search.AlexandriaQuery;
import nl.knaw.huygens.alexandria.endpoint.LocationBuilder;
import nl.knaw.huygens.alexandria.endpoint.search.SearchResult;
import nl.knaw.huygens.alexandria.exception.BadRequestException;
import nl.knaw.huygens.alexandria.exception.NotFoundException;
import nl.knaw.huygens.alexandria.model.*;
import nl.knaw.huygens.alexandria.query.AlexandriaQueryParser;
import nl.knaw.huygens.alexandria.query.ParsedAlexandriaQuery;
import nl.knaw.huygens.alexandria.storage.DumpFormat;
import nl.knaw.huygens.alexandria.storage.Storage;
import nl.knaw.huygens.alexandria.storage.frames.*;
import nl.knaw.huygens.alexandria.textlocator.AlexandriaTextLocator;
import nl.knaw.huygens.alexandria.textlocator.TextLocatorFactory;
import nl.knaw.huygens.alexandria.textlocator.TextLocatorParseException;
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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

@Singleton
public class TinkerPopService implements AlexandriaService {
  private static final TemporalAmount TENTATIVES_TTL = Duration.ofDays(1);

  private Storage storage;
  private final LocationBuilder locationBuilder;
  private final AlexandriaQueryParser alexandriaQueryParser;

  @Inject
  public TinkerPopService(Storage storage, LocationBuilder locationBuilder) {
    // Log.trace("{} created, locationBuilder=[{}]", getClass().getSimpleName(), locationBuilder);
    this.locationBuilder = locationBuilder;
    this.alexandriaQueryParser = new AlexandriaQueryParser(locationBuilder);
    setStorage(storage);
  }

  public void setStorage(Storage storage) {
    this.storage = storage;
    // this.textGraphService = new TextGraphService(storage);
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

  @Override
  public AlexandriaResource createResource(UUID resourceUUID, String ref, TentativeAlexandriaProvenance provenance, AlexandriaState state) {
    return storage.runInTransaction(() -> {
      AlexandriaResource resource = new AlexandriaResource(resourceUUID, provenance);
      resource.setCargo(ref);
      resource.setState(state);
      createOrUpdateResource(resource);
      return resource;
    });
  }

  private Optional<AlexandriaResource> getOptionalResource(UUID uuid) {
    return storage.readVF(ResourceVF.class, uuid).map(this::deframeResource);
  }

  private Optional<AlexandriaResource> getOptionalResourceWithUniqueRef(String ref) {
    FramedGraphTraversal<Object, ResourceVF> traversal = storage.find(ResourceVF.class).has(ResourceVF.Properties.CARGO, ref);
    AlexandriaResource alexandriaResource = traversal.hasNext() ? deframeResourceLite(traversal.next()) : null;
    return Optional.ofNullable(alexandriaResource);
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
  public Optional<AlexandriaResource> readResourceWithUniqueRef(String resourceRef) {
    return storage.runInTransaction(() -> getOptionalResourceWithUniqueRef(resourceRef));
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
  public List<AlexandriaResource> readSubResources(UUID uuid) {
    ResourceVF resourcevf = readExistingResourceVF(uuid);
    return resourcevf.getSubResources().stream()//
        .map(this::deframeResource)//
        .sorted()//
        .collect(toList());
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

    // update the uuid of the (to be) deprecated annotation, so the annotationuuid can be used for the new annotation
    oldAnnotationVF.setUuid(oldAnnotationVF.getUuid() + "." + oldAnnotationVF.getRevision());

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
    throw new NotImplementedException("readAnnotationBody");
  }


  @Override
  public SearchResult execute(AlexandriaQuery query) {
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
    storage.runInTransaction(() -> storage.getVertexTraversal()//
        .forEachRemaining(org.apache.tinkerpop.gremlin.structure.Element::remove));
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

  private void updateState(AlexandriaVF vf, AlexandriaState newState) {
    vf.setState(newState.name());
    vf.setStateSince(Instant.now().getEpochSecond());
  }

  // - private methods -//


  private AlexandriaAnnotation createAnnotation(AlexandriaAnnotationBody annotationbody, TentativeAlexandriaProvenance provenance) {
    return new AlexandriaAnnotation(UUID.randomUUID(), annotationbody, provenance);
  }

  private AlexandriaAnnotation createAnnotation(AlexandriaTextLocator textLocator, AlexandriaAnnotationBody annotationbody, TentativeAlexandriaProvenance provenance) {
    AlexandriaAnnotation alexandriaAnnotation = createAnnotation(annotationbody, provenance);
    alexandriaAnnotation.setLocator(textLocator);
    return alexandriaAnnotation;
  }

  private AlexandriaResource deframeResource(Vertex v) {
    ResourceVF rvf = storage.frameVertex(v, ResourceVF.class);
    return deframeResource(rvf);
  }

  private AlexandriaResource deframeResource(ResourceVF rvf) {
    AlexandriaResource resource = deframeResourceLite(rvf);
    // setTextViews(rvf, resource);

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
    rvf.getSubResources()//
            .forEach(vf -> resource.addSubResourcePointer(new IdentifiablePointer<>(AlexandriaResource.class, vf.getUuid())));
    return resource;
  }

  private AlexandriaResource deframeResourceLite(ResourceVF rvf) {
    TentativeAlexandriaProvenance provenance = deframeProvenance(rvf);
    UUID uuid = getUUID(rvf);
    AlexandriaResource resource = new AlexandriaResource(uuid, provenance);
    resource.setHasText(rvf.getHasText());
    resource.setCargo(rvf.getCargo());
    resource.setState(AlexandriaState.valueOf(rvf.getState()));
    resource.setStateSince(Instant.ofEpochSecond(rvf.getStateSince()));
    return resource;
  }


  private AnnotationVF frameAnnotation(AlexandriaAnnotation newAnnotation) {
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

  protected UUID getUUID(IdentifiableVF vf) {
    return UUID.fromString(vf.getUuid().replaceFirst("\\..$", "")); // remove revision suffix for deprecated annotations
  }

  private List<Map<String, Object>> processQuery(AlexandriaQuery query) {
    ParsedAlexandriaQuery pQuery = alexandriaQueryParser.parse(query);

    Stream<Map<String, Object>> mapStream = pQuery.getResultStreamMapper().apply(storage);

    if (pQuery.isDistinct()) {
      mapStream = mapStream.distinct();
    }
    if (pQuery.doGrouping()) {
      mapStream = mapStream//
        .collect(groupingBy(pQuery::concatenateGroupByFieldsValues))//
        .values().stream()//
        .map(pQuery::collectListFieldValues)//
        .map(this::addListSize);
      if (pQuery.sortOnListSize()){
        mapStream = mapStream.sorted(pQuery.getListSizeComparator());
      }
    }
    return mapStream//
        .collect(toList());
  }

  private Map<String, Object> addListSize(Map<String, Object> resultMap){
    resultMap.put("_list.size", ((List<Object>) resultMap.get("_list")).size());
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
