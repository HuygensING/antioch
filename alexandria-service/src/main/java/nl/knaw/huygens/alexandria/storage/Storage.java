package nl.knaw.huygens.alexandria.storage;

import static java.util.stream.Collectors.toSet;
import static org.apache.tinkerpop.gremlin.process.traversal.P.lt;

import java.io.IOException;
import java.io.OutputStream;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.TemporalAmount;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;

import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Graph.Features;
import org.apache.tinkerpop.gremlin.structure.Graph.Features.GraphFeatures;
import org.apache.tinkerpop.gremlin.structure.Transaction;
import org.apache.tinkerpop.gremlin.structure.io.graphml.GraphMLIo;
import org.apache.tinkerpop.gremlin.structure.io.graphson.GraphSONIo;

import nl.knaw.huygens.Log;
import nl.knaw.huygens.alexandria.exception.NotFoundException;
import nl.knaw.huygens.alexandria.model.Accountable;
import nl.knaw.huygens.alexandria.model.AccountablePointer;
import nl.knaw.huygens.alexandria.model.AlexandriaAnnotation;
import nl.knaw.huygens.alexandria.model.AlexandriaAnnotationBody;
import nl.knaw.huygens.alexandria.model.AlexandriaProvenance;
import nl.knaw.huygens.alexandria.model.AlexandriaResource;
import nl.knaw.huygens.alexandria.model.AlexandriaState;
import nl.knaw.huygens.alexandria.model.TentativeAlexandriaProvenance;
import nl.knaw.huygens.alexandria.storage.frames.AlexandriaVF;
import nl.knaw.huygens.alexandria.storage.frames.AnnotationBodyVF;
import nl.knaw.huygens.alexandria.storage.frames.AnnotationVF;
import nl.knaw.huygens.alexandria.storage.frames.ResourceVF;
import peapod.FramedGraph;
import peapod.FramedGraphTraversal;
import peapod.annotations.Vertex;

public class Storage {
  private static final String IDENTIFIER_PROPERTY = "uuid";
  private static final TemporalAmount TIMEOUT = Duration.ofDays(1);

  private final Graph graph;
  private final FramedGraph framedGraph;

  private Transaction tx;
  private String dumpfile;
  private boolean supportsTransactions;
  private boolean supportsPersistence;

  public Storage(Graph graph) {
    this.graph = graph;
    this.framedGraph = new FramedGraph(graph, ResourceVF.class.getPackage());
    GraphFeatures graphFeatures = graph.features().graph();
    this.supportsPersistence = graphFeatures.supportsPersistence();
    this.supportsTransactions = graphFeatures.supportsTransactions();
  }

  public boolean supportsTransactions() {
    return supportsTransactions;
  }

  public boolean supportsPersistence() {
    return supportsPersistence;
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  public boolean exists(Class clazz, UUID uuid) {
    if (clazz.getAnnotationsByType(Vertex.class).length == 0) {
      throw new RuntimeException("Class " + clazz + " has no peapod @Vertex annotation, are you sure it is the correct class?");
    }
    FramedGraphTraversal fgt = framedGraph.V(clazz).has(IDENTIFIER_PROPERTY, uuid.toString());
    return fgt.tryNext().isPresent();
  }

  public Optional<AlexandriaResource> readResource(UUID uuid) {
    return readResourceVF(uuid).map(this::deframeResource);
  }

  public Optional<AlexandriaAnnotation> readAnnotation(UUID uuid) {
    return readAnnotationVF(uuid).map(this::deframeAnnotation);
  }

  public void createOrUpdateResource(AlexandriaResource resource) {
    startTransaction();

    final ResourceVF rvf;
    final UUID uuid = resource.getId();
    if (exists(ResourceVF.class, uuid)) {
      rvf = readResourceVF(uuid).get();
    } else {
      rvf = framedGraph.addVertex(ResourceVF.class);
      rvf.setUuid(uuid.toString());
    }

    rvf.setCargo(resource.getCargo());

    setAlexandriaVFProperties(rvf, resource);

    commitTransaction();
  }

  public void createSubResource(AlexandriaResource subResource) {
    startTransaction();

    final ResourceVF rvf;
    final UUID uuid = subResource.getId();
    if (exists(ResourceVF.class, uuid)) {
      rvf = readResourceVF(uuid).get();
    } else {
      rvf = framedGraph.addVertex(ResourceVF.class);
      rvf.setUuid(uuid.toString());
    }

    rvf.setCargo(subResource.getCargo());
    final UUID parentId = UUID.fromString(subResource.getParentResourcePointer().get().getIdentifier());
    Optional<ResourceVF> parentVF = readResourceVF(parentId);
    rvf.setParentResource(parentVF.get());

    setAlexandriaVFProperties(rvf, subResource);

    commitTransaction();

  }

  public void createOrUpdateAnnotation(AlexandriaAnnotation annotation) {
    startTransaction();

    final AnnotationVF avf;
    final UUID uuid = annotation.getId();
    if (exists(AnnotationVF.class, uuid)) {
      avf = readAnnotationVF(uuid).get();
    } else {
      avf = framedGraph.addVertex(AnnotationVF.class, uuid);
      avf.setUuid(uuid.toString());
    }

    setAlexandriaVFProperties(avf, annotation);

    commitTransaction();
  }

  public void annotateResourceWithAnnotation(AlexandriaResource resource, AlexandriaAnnotation newAnnotation) {
    startTransaction();

    AnnotationVF avf = createAnnotationVF(newAnnotation);

    ResourceVF resourceToAnnotate = readResourceVF(resource.getId()).get();
    avf.setAnnotatedResource(resourceToAnnotate);

    commitTransaction();
  }

  public void annotateAnnotationWithAnnotation(AlexandriaAnnotation annotation, AlexandriaAnnotation newAnnotation) {
    startTransaction();

    AnnotationVF avf = createAnnotationVF(newAnnotation);
    UUID id = annotation.getId();
    annotate(avf, id);

    commitTransaction();
  }

  public Optional<AlexandriaAnnotationBody> findAnnotationBodyWithTypeAndValue(String type, String value) {
    List<AnnotationBodyVF> results = framedGraph.V(AnnotationBodyVF.class)//
        .has("type", type)//
        .has("value", value)//
        .toList();
    if (results.isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(deframeAnnotationBody(results.get(0)));
  }

  public void storeAnnotationBody(AlexandriaAnnotationBody body) {
    startTransaction();
    frameAnnotationBody(body);
    commitTransaction();
  }

  public Set<AlexandriaResource> readSubResources(UUID uuid) {
    ResourceVF resourcevf = readResourceVF(uuid)//
        .orElseThrow(() -> new NotFoundException("no resource found with uuid " + uuid));
    return resourcevf.getSubResources().stream()//
        .map(this::deframeResource)//
        .collect(toSet());
  }

  public AlexandriaAnnotation deprecateAnnotation(UUID oldAnnotationId, AlexandriaAnnotation tmpAnnotation) {
    startTransaction();

    // check if there's an annotation with the given id
    AnnotationVF oldAnnotationVF = readAnnotationVF(oldAnnotationId)//
        .orElseThrow(annotationNotFound(oldAnnotationId));

    AlexandriaAnnotationBody newBody = tmpAnnotation.getBody();
    Optional<AlexandriaAnnotationBody> optionalBody = findAnnotationBodyWithTypeAndValue(newBody.getType(), newBody.getValue());
    AlexandriaAnnotationBody body;
    if (optionalBody.isPresent()) {
      body = optionalBody.get();
    } else {
      frameAnnotationBody(newBody);
      body = newBody;
    }

    AlexandriaProvenance tmpProvenance = tmpAnnotation.getProvenance();
    TentativeAlexandriaProvenance provenance = new TentativeAlexandriaProvenance(tmpProvenance.getWho(), tmpProvenance.getWhen(), tmpProvenance.getWhy());
    AlexandriaAnnotation newAnnotation = new AlexandriaAnnotation(tmpAnnotation.getId(), body, provenance);
    AnnotationVF newAnnotationVF = createAnnotationVF(newAnnotation);

    AnnotationVF annotatedAnnotation = oldAnnotationVF.getAnnotatedAnnotation();
    if (annotatedAnnotation != null) {
      newAnnotationVF.setAnnotatedAnnotation(annotatedAnnotation);
    } else {
      ResourceVF annotatedResource = oldAnnotationVF.getAnnotatedResource();
      newAnnotationVF.setAnnotatedResource(annotatedResource);
    }
    newAnnotationVF.setDeprecatedAnnotation(oldAnnotationVF);

    AlexandriaAnnotation resultAnnotation = deframeAnnotation(newAnnotationVF);
    commitTransaction();
    return resultAnnotation;
  }

  public void confirmAnnotation(UUID uuid) {
    startTransaction();
    AnnotationVF annotationVF = readAnnotationVF(uuid).orElseThrow(annotationNotFound(uuid));
    updateState(annotationVF, AlexandriaState.CONFIRMED);
    updateState(annotationVF.getBody(), AlexandriaState.CONFIRMED);
    AnnotationVF deprecatedAnnotation = annotationVF.getDeprecatedAnnotation();
    if (deprecatedAnnotation != null && !deprecatedAnnotation.isDeprecated()) {
      updateState(deprecatedAnnotation, AlexandriaState.DEPRECATED);
    }
    commitTransaction();
  }

  public void dumpToGraphSON(OutputStream os) throws IOException {
    graph.io(new GraphSONIo.Builder()).writer().create().writeGraph(os, graph);
  }

  public void dumpToGraphML(OutputStream os) throws IOException {
    graph.io(new GraphMLIo.Builder()).writer().create().writeGraph(os, graph);
  }

  public void loadFromDisk(String file) {
    try {
      graph.io(new GraphMLIo.Builder()).readGraph(file);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public void saveToDisk(String file) {
    if (file != null) {
      try {
        graph.io(new GraphMLIo.Builder()).writeGraph(file);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  public void removeExpiredTentatives() {
    // Tentative vertices should not have any outgoing or incoming edges!!
    Long threshold = Instant.now().minus(TIMEOUT).getEpochSecond();
    startTransaction();
    graph.traversal().V()//
        .has("state", AlexandriaState.TENTATIVE.name())//
        .has("stateSince", lt(threshold))//
        .remove();
    commitTransaction();
  }

  public String getDumpFile() {
    return dumpfile;
  }

  public void setDumpFile(String dumpfile) {
    this.dumpfile = dumpfile;
  }
  // private methods

  private GraphFeatures graphFeatures() {
    return features().graph();
  }

  private Features features() {
    return graph.features();
  }

  private void startTransaction() {
    if (supportsTransactions()) {
      tx = framedGraph.tx();
    }
  }

  private void commitTransaction() {
    if (supportsTransactions()) {
      tx.commit();
      tx.close();
    }
    if (!supportsPersistence()) {
      saveToDisk(getDumpFile());
    }
  }

  private void rollbackTransaction() {
    if (supportsTransactions()) {
      tx.rollback();
      tx.close();
    } else {
      Log.error("rollback called, but transactions are not supported by graph {}", graph);
    }
  }

  private Optional<ResourceVF> readResourceVF(UUID uuid) {
    return firstOrEmpty(framedGraph.V(ResourceVF.class).has(IDENTIFIER_PROPERTY, uuid.toString()).toList());
  }

  private Optional<AnnotationVF> readAnnotationVF(UUID uuid) {
    return firstOrEmpty(framedGraph.V(AnnotationVF.class).has(IDENTIFIER_PROPERTY, uuid.toString()).toList());
  }

  private <T> Optional<T> firstOrEmpty(List<T> results) {
    return results.isEmpty() ? Optional.empty() : Optional.ofNullable(results.get(0));
  }

  private AnnotationVF createAnnotationVF(AlexandriaAnnotation newAnnotation) {
    AnnotationVF avf = framedGraph.addVertex(AnnotationVF.class);
    setAlexandriaVFProperties(avf, newAnnotation);

    String bodyId = newAnnotation.getBody().getId().toString();
    List<AnnotationBodyVF> results = framedGraph.V(AnnotationBodyVF.class).has(IDENTIFIER_PROPERTY, bodyId).toList();
    AnnotationBodyVF bodyVF = results.get(0);

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
      resource.setParentResourcePointer(new AccountablePointer<>(AlexandriaResource.class, parentResource.getUuid()));
    }
    rvf.getSubResources().stream()//
        .forEach(vf -> resource.addSubResourcePointer(new AccountablePointer<>(AlexandriaResource.class, vf.getUuid())));
    return resource;
  }

  private AlexandriaAnnotation deframeAnnotation(AnnotationVF annotationVF) {
    TentativeAlexandriaProvenance provenance = deframeProvenance(annotationVF);
    UUID uuid = getUUID(annotationVF);
    AlexandriaAnnotationBody body = deframeAnnotationBody(annotationVF.getBody());
    AlexandriaAnnotation annotation = new AlexandriaAnnotation(uuid, body, provenance);
    annotation.setState(AlexandriaState.valueOf(annotationVF.getState()));
    annotation.setStateSince(Instant.ofEpochSecond(annotationVF.getStateSince()));
    AnnotationVF annotatedAnnotation = annotationVF.getAnnotatedAnnotation();
    if (annotatedAnnotation == null) {
      ResourceVF annotatedResource = annotationVF.getAnnotatedResource();
      annotation.setAnnotatablePointer(new AccountablePointer<>(AlexandriaResource.class, annotatedResource.getUuid()));
    } else {
      annotation.setAnnotatablePointer(new AccountablePointer<>(AlexandriaAnnotation.class, annotatedAnnotation.getUuid()));
    }
    for (AnnotationVF avf : annotationVF.getAnnotatedBy()) {
      AlexandriaAnnotation annotationAnnotation = deframeAnnotation(avf);
      annotation.addAnnotation(annotationAnnotation);
    }
    return annotation;
  }

  private AnnotationBodyVF frameAnnotationBody(AlexandriaAnnotationBody body) {
    AnnotationBodyVF abvf = framedGraph.addVertex(AnnotationBodyVF.class);
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

  private UUID getUUID(AlexandriaVF vf) {
    return UUID.fromString(vf.getUuid());
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

  private void updateState(AlexandriaVF vf, AlexandriaState newState) {
    vf.setState(newState.name());
    vf.setStateSince(Instant.now().getEpochSecond());
  }

  private void annotate(AnnotationVF avf, UUID id) {
    AnnotationVF annotationToAnnotate = readAnnotationVF(id).get();
    avf.setAnnotatedAnnotation(annotationToAnnotate);
  }

  private Supplier<NotFoundException> annotationNotFound(UUID oldAnnotationId) {
    return () -> new NotFoundException("no annotation found with uuid " + oldAnnotationId);
  }
}
