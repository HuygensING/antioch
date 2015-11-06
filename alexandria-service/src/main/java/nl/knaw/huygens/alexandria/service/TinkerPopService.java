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

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import java.io.IOException;
import java.io.OutputStream;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.TemporalAmount;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.apache.commons.lang.NotImplementedException;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Maps;

import nl.knaw.huygens.Log;
import nl.knaw.huygens.alexandria.endpoint.LocationBuilder;
import nl.knaw.huygens.alexandria.endpoint.search.SearchResult;
import nl.knaw.huygens.alexandria.exception.BadRequestException;
import nl.knaw.huygens.alexandria.exception.NotFoundException;
import nl.knaw.huygens.alexandria.model.Accountable;
import nl.knaw.huygens.alexandria.model.AlexandriaAnnotation;
import nl.knaw.huygens.alexandria.model.AlexandriaAnnotationBody;
import nl.knaw.huygens.alexandria.model.AlexandriaProvenance;
import nl.knaw.huygens.alexandria.model.AlexandriaResource;
import nl.knaw.huygens.alexandria.model.AlexandriaState;
import nl.knaw.huygens.alexandria.model.IdentifiablePointer;
import nl.knaw.huygens.alexandria.model.TentativeAlexandriaProvenance;
import nl.knaw.huygens.alexandria.model.search.AlexandriaQuery;
import nl.knaw.huygens.alexandria.query.AlexandriaQueryParser;
import nl.knaw.huygens.alexandria.query.ParsedAlexandriaQuery;
import nl.knaw.huygens.alexandria.storage.DumpFormat;
import nl.knaw.huygens.alexandria.storage.Storage;
import nl.knaw.huygens.alexandria.storage.frames.AlexandriaVF;
import nl.knaw.huygens.alexandria.storage.frames.AnnotationBodyVF;
import nl.knaw.huygens.alexandria.storage.frames.AnnotationVF;
import nl.knaw.huygens.alexandria.storage.frames.ResourceVF;

public class TinkerPopService implements AlexandriaService {
  private static final TemporalAmount TENTATIVES_TTL = Duration.ofDays(1);

  private Storage storage;

  private LocationBuilder locationBuilder;

  private AlexandriaQueryParser alexandriaQueryParser;

  @Inject
  public TinkerPopService(Storage storage, LocationBuilder locationBuilder) {
    Log.trace("{} created, locationBuilder=[{}]", getClass().getSimpleName(), locationBuilder);
    this.locationBuilder = locationBuilder;
    this.alexandriaQueryParser = new AlexandriaQueryParser(locationBuilder);
    setStorage(storage);
  }

  public void setStorage(Storage storage) {
    this.storage = storage;
  }

  // - AlexandriaService methods -//
  // all public methods that interact with storage should start with storage.startTransaction()
  // and end with storage.commitTransaction() for write actions
  // and storage.rollbackTransaction() for read-only actions

  @Override
  public boolean createOrUpdateResource(UUID uuid, String ref, TentativeAlexandriaProvenance provenance, AlexandriaState state) {
    storage.startTransaction();
    AlexandriaResource resource;
    boolean newlyCreated;

    if (storage.existsVF(ResourceVF.class, uuid)) {
      resource = readResource(uuid).get();
      newlyCreated = false;

    } else {
      resource = new AlexandriaResource(uuid, provenance);
      newlyCreated = true;
    }
    resource.setCargo(ref);
    resource.setState(state);
    createOrUpdateResource(resource);
    storage.commitTransaction();
    return newlyCreated;
  }

  @Override
  public AlexandriaAnnotation annotate(AlexandriaResource resource, AlexandriaAnnotationBody annotationbody, TentativeAlexandriaProvenance provenance) {
    AlexandriaAnnotation newAnnotation = createAnnotation(annotationbody, provenance);
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
    storage.startTransaction();
    Optional<AlexandriaResource> optionalResource = storage.readVF(ResourceVF.class, uuid).map(this::deframeResource);
    storage.rollbackTransaction();
    return optionalResource;
  }

  @Override
  public Optional<AlexandriaAnnotation> readAnnotation(UUID uuid) {
    storage.startTransaction();
    Optional<AlexandriaAnnotation> optionalAnnotation = storage.readVF(AnnotationVF.class, uuid).map(this::deframeAnnotation);
    storage.rollbackTransaction();
    return optionalAnnotation;
  }

  @Override
  public Optional<AlexandriaAnnotation> readAnnotation(UUID uuid, Integer revision) {
    Optional<AlexandriaAnnotation> optionalAnnotation;
    storage.startTransaction();
    Optional<AnnotationVF> versionedAnnotation = storage.readVF(AnnotationVF.class, uuid, revision);
    if (versionedAnnotation.isPresent()) {
      optionalAnnotation = versionedAnnotation.map(this::deframeAnnotation);
    } else {
      Optional<AnnotationVF> currentAnnotation = storage.readVF(AnnotationVF.class, uuid);
      if (currentAnnotation.isPresent() && currentAnnotation.get().getRevision().equals(revision)) {
        optionalAnnotation = currentAnnotation.map(this::deframeAnnotation);
      } else {
        optionalAnnotation = Optional.empty();
      }
    }
    storage.rollbackTransaction();
    return optionalAnnotation;
  }

  @Override
  public TemporalAmount getTentativesTimeToLive() {
    return TENTATIVES_TTL;
  }

  @Override
  public void removeExpiredTentatives() {
    // Tentative vertices should not have any outgoing or incoming edges!!
    Long threshold = Instant.now().minus(TENTATIVES_TTL).getEpochSecond();
    storage.startTransaction();
    storage.removeExpiredTentatives(threshold);
    storage.commitTransaction();
  }

  @Override
  public Optional<AlexandriaAnnotationBody> findAnnotationBodyWithTypeAndValue(String type, String value) {
    storage.startTransaction();
    final List<AnnotationBodyVF> results = storage.find(AnnotationBodyVF.class)//
        .has("type", type)//
        .has("value", value)//
        .toList();
    storage.rollbackTransaction();
    if (results.isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(deframeAnnotationBody(results.get(0)));
  }

  @Override
  public Optional<AlexandriaResource> findSubresourceWithSubAndParentId(String sub, UUID parentId) {
    // TODO: find the gremlin way to do this in one:
    // in cypher: match (r:Resource{uuid:parentId})<-[:PART_OF]-(s:Resource{cargo:sub}) return s.uuid
    storage.startTransaction();
    final List<ResourceVF> results = storage.find(ResourceVF.class)//
        .has("cargo", sub)//
        .toList();
    storage.rollbackTransaction();

    if (results.isEmpty()) {
      return Optional.empty();
    }

    results.stream()//
        .filter(r -> r.getParentResource() != null//
            && r.getParentResource().getUuid().equals(parentId.toString()))//
        .collect(toList());
    return Optional.of(deframeResource(results.get(0)));
  }

  @Override
  public Set<AlexandriaResource> readSubResources(UUID uuid) {
    storage.startTransaction();
    ResourceVF resourcevf = storage.readVF(ResourceVF.class, uuid)//
        .orElseThrow(() -> new NotFoundException("no resource found with uuid " + uuid));
    storage.rollbackTransaction();
    return resourcevf.getSubResources().stream()//
        .map(this::deframeResource)//
        .collect(toSet());
  }

  @Override
  public AlexandriaAnnotation deprecateAnnotation(UUID annotationId, AlexandriaAnnotation updatedAnnotation) {
    storage.startTransaction();

    // check if there's an annotation with the given id
    AnnotationVF oldAnnotationVF = storage.readVF(AnnotationVF.class, annotationId)//
        .orElseThrow(annotationNotFound(annotationId));
    if (oldAnnotationVF.isTentative()) {
      throw incorrectStateException(annotationId, "tentative");
    } else if (oldAnnotationVF.isDeleted()) {
      throw new BadRequestException("annotation " + annotationId + " is " + "deleted");
    } else if (oldAnnotationVF.isDeprecated()) {
      throw new BadRequestException("annotation " + annotationId + " is " + "already deprecated");
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

    AlexandriaAnnotation resultAnnotation = deframeAnnotation(newAnnotationVF);
    storage.commitTransaction();
    return resultAnnotation;
  }

  @Override
  public void confirmResource(UUID uuid) {
    storage.startTransaction();
    ResourceVF resourceVF = storage.readVF(ResourceVF.class, uuid)//
        .orElseThrow(resourceNotFound(uuid));
    updateState(resourceVF, AlexandriaState.CONFIRMED);
    storage.commitTransaction();
  }

  @Override
  public void confirmAnnotation(UUID uuid) {
    storage.startTransaction();
    AnnotationVF annotationVF = storage.readVF(AnnotationVF.class, uuid)//
        .orElseThrow(annotationNotFound(uuid));
    updateState(annotationVF, AlexandriaState.CONFIRMED);
    updateState(annotationVF.getBody(), AlexandriaState.CONFIRMED);
    AnnotationVF deprecatedAnnotation = annotationVF.getDeprecatedAnnotation();
    if (deprecatedAnnotation != null && !deprecatedAnnotation.isDeprecated()) {
      updateState(deprecatedAnnotation, AlexandriaState.DEPRECATED);
    }
    storage.commitTransaction();
  }

  @Override
  public void deleteAnnotation(AlexandriaAnnotation annotation) {
    storage.startTransaction();
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
    storage.commitTransaction();
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
  public SearchResult execute(AlexandriaQuery query) {
    Stopwatch stopwatch = Stopwatch.createStarted();
    List<Map<String, Object>> processQuery = processQuery(query);
    stopwatch.stop();
    long elapsedMillis = stopwatch.elapsed(TimeUnit.MILLISECONDS);

    return new SearchResult(locationBuilder)//
        .setId(UUID.randomUUID())//
        .setQuery(query)//
        .setSearchDurationInMilliseconds(elapsedMillis)//
        .setResults(processQuery);
  }

  @Override
  public void exportDb(String format, String filename) {
    storage.startTransaction();
    try {
      storage.writeGraph(DumpFormat.valueOf(format), filename);
    } catch (IOException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    } finally {
      storage.rollbackTransaction();
    }
  }

  @Override
  public void importDb(String format, String filename) {
    try {
      storage = clearGraph();
      storage.startTransaction();
      storage.readGraph(DumpFormat.valueOf(format), filename);
    } catch (IOException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    } finally {
      storage.commitTransaction();
    }
  }

  Storage clearGraph() {
    storage.getVertexTraversal().forEachRemaining((Consumer<Vertex>) vertex -> vertex.remove());
    return storage;
  }

  // - other public methods -//

  public void createSubResource(AlexandriaResource subResource) {
    storage.startTransaction();

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

    storage.commitTransaction();
  }

  public void createOrUpdateAnnotation(AlexandriaAnnotation annotation) {
    storage.startTransaction();

    final AnnotationVF avf;
    final UUID uuid = annotation.getId();
    if (storage.existsVF(AnnotationVF.class, uuid)) {
      avf = storage.readVF(AnnotationVF.class, uuid).get();
    } else {
      avf = storage.createVF(AnnotationVF.class);
      avf.setUuid(uuid.toString());
    }

    setAlexandriaVFProperties(avf, annotation);

    storage.commitTransaction();
  }

  public void annotateResourceWithAnnotation(AlexandriaResource resource, AlexandriaAnnotation newAnnotation) {
    storage.startTransaction();

    AnnotationVF avf = frameAnnotation(newAnnotation);

    ResourceVF resourceToAnnotate = storage.readVF(ResourceVF.class, resource.getId()).get();
    avf.setAnnotatedResource(resourceToAnnotate);

    storage.commitTransaction();
  }

  public void storeAnnotationBody(AlexandriaAnnotationBody body) {
    storage.startTransaction();
    frameAnnotationBody(body);
    storage.commitTransaction();
  }

  public void annotateAnnotationWithAnnotation(AlexandriaAnnotation annotation, AlexandriaAnnotation newAnnotation) {
    storage.startTransaction();

    AnnotationVF avf = frameAnnotation(newAnnotation);
    UUID id = annotation.getId();
    annotate(avf, id);

    storage.commitTransaction();
  }

  public void dumpToGraphSON(OutputStream os) throws IOException {
    storage.dumpToGraphSON(os);
  }

  public void dumpToGraphML(OutputStream os) throws IOException {
    storage.dumpToGraphML(os);
  }

  // - package methods -//

  void createOrUpdateResource(AlexandriaResource resource) {
    final ResourceVF rvf;
    final UUID uuid = resource.getId();
    if (storage.existsVF(ResourceVF.class, uuid)) {
      rvf = storage.readVF(ResourceVF.class, uuid).get();
    } else {
      rvf = storage.createVF(ResourceVF.class);
      rvf.setUuid(uuid.toString());
    }

    rvf.setCargo(resource.getCargo());

    setAlexandriaVFProperties(rvf, resource);
  }

  // - private methods -//

  private AlexandriaAnnotation createAnnotation(AlexandriaAnnotationBody annotationbody, TentativeAlexandriaProvenance provenance) {
    UUID id = UUID.randomUUID();
    return new AlexandriaAnnotation(id, annotationbody, provenance);
  }

  AnnotationVF frameAnnotation(AlexandriaAnnotation newAnnotation) {
    AnnotationVF avf = storage.createVF(AnnotationVF.class);
    setAlexandriaVFProperties(avf, newAnnotation);
    avf.setRevision(newAnnotation.getRevision());

    UUID bodyId = newAnnotation.getBody().getId();
    AnnotationBodyVF bodyVF = storage.readVF(AnnotationBodyVF.class, bodyId).get();
    avf.setBody(bodyVF);
    return avf;
  }

  private AlexandriaResource deframeResource(ResourceVF rvf) {
    TentativeAlexandriaProvenance provenance = deframeProvenance(rvf);
    UUID uuid = getUUID(rvf);
    AlexandriaResource resource = new AlexandriaResource(uuid, provenance);
    resource.setCargo(rvf.getCargo());
    resource.setState(AlexandriaState.valueOf(rvf.getState()));
    resource.setStateSince(Instant.ofEpochSecond(rvf.getStateSince()));
    for (AnnotationVF annotationVF : rvf.getAnnotatedBy()) {
      AlexandriaAnnotation annotation = deframeAnnotation(annotationVF);
      resource.addAnnotation(annotation);
    }
    ResourceVF parentResource = rvf.getParentResource();
    if (parentResource != null) {
      resource.setParentResourcePointer(new IdentifiablePointer<>(AlexandriaResource.class, parentResource.getUuid()));
    }
    rvf.getSubResources().stream()//
        .forEach(vf -> resource.addSubResourcePointer(new IdentifiablePointer<>(AlexandriaResource.class, vf.getUuid())));
    return resource;
  }

  private AlexandriaAnnotation deframeAnnotation(AnnotationVF annotationVF) {
    TentativeAlexandriaProvenance provenance = deframeProvenance(annotationVF);
    UUID uuid = getUUID(annotationVF);
    AlexandriaAnnotationBody body = deframeAnnotationBody(annotationVF.getBody());
    AlexandriaAnnotation annotation = new AlexandriaAnnotation(uuid, body, provenance);
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

  void updateState(AlexandriaVF vf, AlexandriaState newState) {
    vf.setState(newState.name());
    vf.setStateSince(Instant.now().getEpochSecond());
  }

  void annotate(AnnotationVF avf, UUID id) {
    AnnotationVF annotationToAnnotate = storage.readVF(AnnotationVF.class, id).get();
    avf.setAnnotatedAnnotation(annotationToAnnotate);
  }

  private List<Map<String, Object>> processQuery(AlexandriaQuery query) {
    ParsedAlexandriaQuery pQuery = alexandriaQueryParser.parse(query);

    Predicate<AnnotationVF> predicate = pQuery.getPredicate();
    Comparator<AnnotationVF> comparator = pQuery.getResultComparator();
    Function<AnnotationVF, Map<String, Object>> mapper = pQuery.getResultMapper();

    Stream<AnnotationVF> stream = pQuery.getAnnotationVFFinder().apply(storage);
    Log.debug("list={}", stream);

    Stream<Map<String, Object>> mapStream = stream//
        .filter(predicate)//
        .sorted(comparator)//
        .map(mapper);
    if (pQuery.isDistinct()) {
      mapStream = mapStream.distinct();
    }
    List<Map<String, Object>> results = mapStream//
        .collect(toList());
    Log.debug("results={}", results);
    return results;
  }

  // @SuppressWarnings("unchecked")
  // private void findAllConfirmedAnnotationsRelatedToResource(String uuid) {
  // // case: find all annotations related to a given resource (or its subresources)
  // // start with the resource
  // // from there: find subresources, add them to resource list
  // // foreach resource in the list, get the confirmed annotations of that resource, add them to annotations list
  // // foreach annotation in the annotationlist, find the confirmed annotations of that annotation, add them to annotations list
  // GraphTraversal<Vertex, Vertex> traversal = storage.getVertexTraversal();
  // traversal.has("uuid", uuid).in(ResourceVF.PART_OF).in("annotates").has("state", "CONFIRMED").as("a").out("has_body").as("b").toList();
  // traversal.has("uuid", uuid)
  // .union(//
  // __.in(ResourceVF.PART_OF).in(AnnotationVF.ANNOTATES_RESOURCE), //
  // __.in(AnnotationVF.ANNOTATES_RESOURCE)//
  // ).has("state", AlexandriaState.CONFIRMED.name()).as("a").out(AnnotationVF.HAS_BODY).as("b").toList();
  // }

  @Override
  public Map<String, Object> getMetadata() {
    Map<String, Object> metadata = Maps.newLinkedHashMap();
    metadata.put("type", this.getClass().getCanonicalName());
    metadata.put("storage", storage.getMetadata());
    return metadata;
  }

  @Override
  public void destroy() {
    // Log.info("destroy called");
    storage.destroy();
    // Log.info("destroy done");
  }

  @Override
  public void setResourceText(UUID resourceUUID, String body) {

  }

  @Override
  public String getResourceTextAsPlainText(UUID resourceUUID) {
    return null;
  }

}
