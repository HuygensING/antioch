package nl.knaw.huygens.alexandria.storage;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.UUID;

import nl.knaw.huygens.Log;
import nl.knaw.huygens.alexandria.model.Accountable;
import nl.knaw.huygens.alexandria.model.AlexandriaAnnotation;
import nl.knaw.huygens.alexandria.model.AlexandriaAnnotationBody;
import nl.knaw.huygens.alexandria.model.AlexandriaProvenance;
import nl.knaw.huygens.alexandria.model.AlexandriaResource;
import nl.knaw.huygens.alexandria.model.TentativeAlexandriaProvenance;
import nl.knaw.huygens.alexandria.storage.frames.AlexandriaVF;
import nl.knaw.huygens.alexandria.storage.frames.AnnotationVF;
import nl.knaw.huygens.alexandria.storage.frames.ResourceVF;

import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Transaction;
import org.apache.tinkerpop.gremlin.structure.io.graphson.GraphSONIo;

import peapod.FramedGraph;

public class Storage {
  private static Graph g;
  private static FramedGraph fg;
  private static boolean transactionsSupported;

  private Transaction tx;

  public Storage(Graph graph) {
    g = graph;
    transactionsSupported = g.features().graph().supportsTransactions();
    fg = new FramedGraph(g, ResourceVF.class.getPackage());
  }

  public boolean exists(Class clazz, UUID uuid) {
    // List<AlexandriaVF> results = fg.V(clazz).has("id", uuid.toString()).toList();
    Object v = fg.v(uuid, clazz);
    return v != null;
  }

  public AlexandriaResource readResource(UUID uuid) {
    ResourceVF rvf = readResourceVF(uuid);
    if (rvf == null) {
      return null;
    }
    TentativeAlexandriaProvenance provenance = createTentativeProvenance(rvf);
    AlexandriaResource resource = new AlexandriaResource(uuid, provenance);
    resource.setRef(rvf.getRef());
    resource.setState(rvf.getState());
    return resource;
  }

  public AlexandriaAnnotation readAnnotation(UUID uuid) {
    AnnotationVF avf = readAnnotationVF(uuid);
    if (avf == null) {
      return null;
    }
    TentativeAlexandriaProvenance provenance = createTentativeProvenance(avf);
    AlexandriaAnnotationBody body = new AlexandriaAnnotationBody(uuid, avf.getType(), avf.getValue(), provenance);
    AlexandriaAnnotation annotation = new AlexandriaAnnotation(uuid, body, provenance);
    return annotation;
  }

  public void createOrUpdateResource(AlexandriaResource resource) {
    startTransaction();

    ResourceVF rvf = null;
    if (exists(resource.getClass(), resource.getId())) {
      rvf = fg.v(resource.getId());
    } else {
      rvf = fg.addVertex(ResourceVF.class, resource.getId());
    }

    rvf.setRef(resource.getRef());
    rvf.setState(resource.getState());

    setAlexandriaVFProperties(resource, rvf);

    commitTransaction();
  }

  public void createOrUpdateAnnotation(AlexandriaAnnotation annotation) {
    startTransaction();

    AnnotationVF avf = null;
    if (exists(annotation.getClass(), annotation.getId())) {
      avf = fg.v(annotation.getId());
    } else {
      avf = fg.addVertex(AnnotationVF.class, annotation.getId());
    }

    avf.setType(annotation.getBody().getType());
    avf.setValue(annotation.getBody().getValue());
    avf.setState(annotation.getState());

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

  public void dump(OutputStream os) throws IOException {
    g.io(new GraphSONIo.Builder()).writer().create().writeGraph(os, g);
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
    List<ResourceVF> results = fg.V(ResourceVF.class).has("id", uuid.toString()).toList();
    ResourceVF rvf = null;
    if (!results.isEmpty()) {
      rvf = results.get(0);
    }
    return rvf;
  }

  private AnnotationVF readAnnotationVF(UUID uuid) {
    List<AnnotationVF> results = fg.V(AnnotationVF.class).has("id", uuid.toString()).toList();
    AnnotationVF vf = null;
    if (!results.isEmpty()) {
      vf = results.get(0);
    }
    return vf;
  }

  private TentativeAlexandriaProvenance createTentativeProvenance(AlexandriaVF avf) {
    return new TentativeAlexandriaProvenance(avf.getProvenanceWho(), avf.getProvenanceWhen(), avf.getProvenanceWhy());
  }

  private AnnotationVF createAnnotationVF(AlexandriaAnnotation newAnnotation) {
    AnnotationVF avf = fg.addVertex(AnnotationVF.class, newAnnotation.getId());
    setAlexandriaVFProperties(newAnnotation, avf);

    avf.setType(newAnnotation.getBody().getType());
    avf.setValue(newAnnotation.getBody().getValue());
    avf.setState(newAnnotation.getState());
    return avf;
  }

  private void setAlexandriaVFProperties(Accountable resource, AlexandriaVF vf) {
    vf.setId(resource.getId().toString());

    AlexandriaProvenance provenance = resource.getProvenance();
    vf.setProvenanceWhen(provenance.getWhen());
    vf.setProvenanceWho(provenance.getWho());
    vf.setProvenanceWhy(provenance.getWhy());
  }
}
