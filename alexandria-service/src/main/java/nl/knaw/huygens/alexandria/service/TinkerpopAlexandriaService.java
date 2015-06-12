package nl.knaw.huygens.alexandria.service;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import javax.inject.Inject;

import nl.knaw.huygens.alexandria.model.AlexandriaAnnotation;
import nl.knaw.huygens.alexandria.model.AlexandriaAnnotationBody;
import nl.knaw.huygens.alexandria.model.AlexandriaProvenance;
import nl.knaw.huygens.alexandria.model.AlexandriaResource;
import nl.knaw.huygens.alexandria.model.TentativeAlexandriaProvenance;
import nl.knaw.huygens.alexandria.storage.Storage;

public class TinkerpopAlexandriaService implements AlexandriaService {
  private Storage storage;

  @Inject
  public TinkerpopAlexandriaService(Storage storage) {
    this.storage = storage;
  }

  @Override
  public boolean createOrUpdateResource(UUID uuid, String ref, TentativeAlexandriaProvenance provenance) {
    AlexandriaResource resource;
    boolean newlyCreated;

    if (storage.exists(AlexandriaResource.class, uuid)) {
      resource = readResource(uuid);
      newlyCreated = false;

    } else {
      resource = new AlexandriaResource(uuid, provenance);
      newlyCreated = true;
    }
    resource.setRef(ref);
    storage.createOrUpdateResource(resource);

    return newlyCreated;
  }

  @Override
  public AlexandriaResource readResource(UUID uuid) {
    return storage.readResource(uuid);
  }

  @Override
  public boolean createAnnotationBody(UUID uuid, Optional<String> type, String value, TentativeAlexandriaProvenance provenance) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public AlexandriaAnnotationBody findAnnotationBodyWithTypeAndValue(Optional<String> type, String value) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public AlexandriaAnnotationBody readAnnotationBody(UUID uuid) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public AlexandriaAnnotation annotate(AlexandriaResource resource, AlexandriaAnnotationBody annotationbody, TentativeAlexandriaProvenance provenance) {
    AlexandriaAnnotation newAnnotation = createAnnotation(annotationbody, provenance);
    storage.annotateResourceWithAnnotation(resource, newAnnotation);
    return newAnnotation;
  }

  private AlexandriaAnnotation createAnnotation(AlexandriaAnnotationBody annotationbody, TentativeAlexandriaProvenance provenance) {
    UUID id = UUID.randomUUID();
    AlexandriaAnnotation newAnnotation = new AlexandriaAnnotation(id, annotationbody, provenance);
    return newAnnotation;
  }

  @Override
  public AlexandriaAnnotation annotate(AlexandriaAnnotation annotation, AlexandriaAnnotationBody annotationbody, TentativeAlexandriaProvenance provenance) {
    AlexandriaAnnotation newAnnotation = createAnnotation(annotationbody, provenance);
    storage.annotateAnnotationWithAnnotation(annotation, newAnnotation);
    return newAnnotation;
  }

  @Override
  public AlexandriaAnnotation readAnnotation(UUID uuid) {
    return storage.readAnnotation(uuid);
  }

  @Override
  public Set<AlexandriaResource> readSubResources(UUID uuid) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public AlexandriaProvenance readProvenance(UUID value) {
    // TODO Auto-generated method stub
    return null;
  }

}
