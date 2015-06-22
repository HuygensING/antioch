package nl.knaw.huygens.alexandria.storage;

import java.io.IOException;
import java.io.OutputStream;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import nl.knaw.huygens.Log;
import nl.knaw.huygens.alexandria.model.Accountable;
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

import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Graph.Features.GraphFeatures;
import org.apache.tinkerpop.gremlin.structure.Transaction;
import org.apache.tinkerpop.gremlin.structure.io.graphson.GraphSONIo;

import peapod.FramedGraph;
import peapod.FramedGraphTraversal;
import peapod.annotations.Vertex;

public class Storage {
  private static final String IDENTIFIER_PROPERTY = "uuid";
  protected static final String DUMPFILE = "dump.json";
  private static Graph g;
  private static FramedGraph fg;
  private static boolean transactionsSupported;
  protected static boolean persistenceSupported;

  private Transaction tx;

  public Storage(Graph graph) {
    g = graph;
    GraphFeatures features = g.features().graph();
    transactionsSupported = features.supportsTransactions();
    persistenceSupported = features.supportsPersistence();
    fg = new FramedGraph(g, ResourceVF.class.getPackage());
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  public boolean exists(Class clazz, UUID uuid) {
    if (clazz.getAnnotationsByType(Vertex.class).length == 0) {
      throw new RuntimeException("Class " + clazz + " has no peapod @Vertex annotation, are you sure it is the correct class?");
    }
    FramedGraphTraversal fgt = fg.V(clazz).has(IDENTIFIER_PROPERTY, uuid.toString());
    return fgt.tryNext().isPresent();
  }

  public AlexandriaResource readResource(UUID uuid) {
    ResourceVF rvf = readResourceVF(uuid);
    if (rvf == null) {
      return null;
    }
    TentativeAlexandriaProvenance provenance = deframeProvenance(rvf);
    AlexandriaResource resource = new AlexandriaResource(uuid, provenance);
    resource.setRef(rvf.getRef());
    resource.setState(AlexandriaState.valueOf(rvf.getState()));
    List<AnnotationVF> annotatedBy = rvf.getAnnotatedBy();
    for (AnnotationVF annotationVF : annotatedBy) {
      AlexandriaAnnotation annotation = deframeAnnotation(annotationVF);
      resource.addAnnotation(annotation);
    }
    return resource;
  }

  public AlexandriaAnnotation readAnnotation(UUID uuid) {
    AnnotationVF avf = readAnnotationVF(uuid);
    if (avf == null) {
      return null;
    }
    return deframeAnnotation(avf);
  }

  public void createOrUpdateResource(AlexandriaResource resource) {
    startTransaction();

    ResourceVF rvf = null;
    UUID uuid = resource.getId();
    if (exists(ResourceVF.class, uuid)) {
      rvf = readResourceVF(uuid);
    } else {
      rvf = fg.addVertex(ResourceVF.class);
      rvf.setUuid(uuid.toString());
    }

    rvf.setRef(resource.getRef());
    rvf.setState(resource.getState().toString());

    setAlexandriaVFProperties(resource, rvf);

    commitTransaction();
  }

  public void createOrUpdateAnnotation(AlexandriaAnnotation annotation) {
    startTransaction();

    AnnotationVF avf = null;
    UUID uuid = annotation.getId();
    if (exists(AnnotationVF.class, uuid)) {
      avf = readAnnotationVF(uuid);
    } else {
      avf = fg.addVertex(AnnotationVF.class, uuid);
      avf.setUuid(uuid.toString());
    }

    // avf.setType(annotation.getBody().getType());
    // avf.setValue(annotation.getBody().getValue());
    avf.setState(annotation.getState().toString());

    setAlexandriaVFProperties(annotation, avf);

    commitTransaction();
  }

  public void annotateResourceWithAnnotation(AlexandriaResource resource, AlexandriaAnnotation newAnnotation) {
    startTransaction();

    AnnotationVF avf = createAnnotationVF(newAnnotation);

    ResourceVF resourceToAnnotate = readResourceVF(resource.getId());
    avf.setAnnotatedResource(resourceToAnnotate);

    commitTransaction();
  }

  public void annotateAnnotationWithAnnotation(AlexandriaAnnotation annotation, AlexandriaAnnotation newAnnotation) {
    startTransaction();

    AnnotationVF avf = createAnnotationVF(newAnnotation);

    AnnotationVF annotationToAnnotate = readAnnotationVF(annotation.getId());
    avf.setAnnotatedAnnotation(annotationToAnnotate);

    commitTransaction();
  }

  public Optional<AlexandriaAnnotationBody> findAnnotationBodyWithTypeAndValue(Optional<String> type, String value) {
    List<AnnotationBodyVF> results = fg.V(AnnotationBodyVF.class).has("type", type.orElse("")).has("value", value).toList();
    if (results.isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(deframeAnnotationBody(results.get(0)));
  }

  public void writeAnnotationBody(AlexandriaAnnotationBody body) {
    AnnotationBodyVF abvf = fg.addVertex(AnnotationBodyVF.class);
    setAlexandriaVFProperties(body, abvf);
    abvf.setType(body.getType());
    abvf.setValue(body.getValue());
  }

  public void dump(OutputStream os) throws IOException {
    g.io(new GraphSONIo.Builder()).writer().create().writeGraph(os, g);
  }

  public void loadFromDisk(String file) {
    try {
      g.io(new GraphSONIo.Builder()).readGraph(file);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public void saveToDisk(String file) {
    try {
      g.io(new GraphSONIo.Builder()).writeGraph(file);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  // private methods

  private void startTransaction() {
    if (transactionsSupported) {
      tx = fg.tx();
    }
  }

  private void commitTransaction() {
    if (transactionsSupported) {
      tx.commit();
      tx.close();
    }
    if (!persistenceSupported) {
      saveToDisk(DUMPFILE);
    }
  }

  private void rollbackTransaction() {
    if (transactionsSupported) {
      tx.rollback();
      tx.close();
    } else {
      Log.error("rollback called, but transactions are not supported by graph {}", g);
    }
  }

  private ResourceVF readResourceVF(UUID uuid) {
    List<ResourceVF> results = fg.V(ResourceVF.class).has(IDENTIFIER_PROPERTY, uuid.toString()).toList();
    ResourceVF rvf = null;
    if (!results.isEmpty()) {
      rvf = results.get(0);
    }
    return rvf;
  }

  private AnnotationVF readAnnotationVF(UUID uuid) {
    List<AnnotationVF> results = fg.V(AnnotationVF.class).has(IDENTIFIER_PROPERTY, uuid.toString()).toList();
    AnnotationVF vf = null;
    if (!results.isEmpty()) {
      vf = results.get(0);
    }
    return vf;
  }

  private AnnotationVF createAnnotationVF(AlexandriaAnnotation newAnnotation) {
    AnnotationVF avf = fg.addVertex(AnnotationVF.class);
    setAlexandriaVFProperties(newAnnotation, avf);

    avf.setState(newAnnotation.getState().toString());

    String bodyId = newAnnotation.getBody().getId().toString();
    List<AnnotationBodyVF> results = fg.V(AnnotationBodyVF.class).has(IDENTIFIER_PROPERTY, bodyId).toList();
    AnnotationBodyVF bodyVF = results.get(0);

    avf.setBody(bodyVF);
    return avf;
  }

  private AlexandriaAnnotation deframeAnnotation(AnnotationVF annotationVF) {
    TentativeAlexandriaProvenance provenance = deframeProvenance(annotationVF);
    UUID uuid = getUUID(annotationVF);
    AlexandriaAnnotationBody body = deframeAnnotationBody(annotationVF.getBody());
    return new AlexandriaAnnotation(uuid, body, provenance);
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

  private void setAlexandriaVFProperties(Accountable resource, AlexandriaVF vf) {
    vf.setUuid(resource.getId().toString());

    AlexandriaProvenance provenance = resource.getProvenance();
    vf.setProvenanceWhen(provenance.getWhen().toString());
    vf.setProvenanceWho(provenance.getWho());
    vf.setProvenanceWhy(provenance.getWhy());
  }
}
