package nl.knaw.huygens.alexandria.service;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import javax.inject.Inject;

import nl.knaw.huygens.alexandria.model.Accountable;
import nl.knaw.huygens.alexandria.model.AccountablePointer;
import nl.knaw.huygens.alexandria.model.AlexandriaAnnotation;
import nl.knaw.huygens.alexandria.model.AlexandriaAnnotationBody;
import nl.knaw.huygens.alexandria.model.AlexandriaResource;
import nl.knaw.huygens.alexandria.model.TentativeAlexandriaProvenance;
import nl.knaw.huygens.alexandria.storage.Storage;
import nl.knaw.huygens.alexandria.storage.frames.ResourceVF;

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

    if (storage.exists(ResourceVF.class, uuid)) {
      resource = readResource(uuid).get();
      newlyCreated = false;

    } else {
      resource = new AlexandriaResource(uuid, provenance);
      newlyCreated = true;
    }
    resource.setCargo(ref);
    storage.createOrUpdateResource(resource);

    return newlyCreated;
  }

  @Override
  public Optional<AlexandriaResource> readResource(UUID uuid) {
    return storage.readResource(uuid);
  }

  @Override
  public AlexandriaAnnotationBody createAnnotationBody(UUID uuid, Optional<String> type, String value, TentativeAlexandriaProvenance provenance) {
    AlexandriaAnnotationBody body = new AlexandriaAnnotationBody(uuid, type.orElse(""), value, provenance);
    storage.writeAnnotationBody(body);
    return body;
  }

  @Override
  public Optional<AlexandriaAnnotationBody> findAnnotationBodyWithTypeAndValue(Optional<String> type, String value) {
    return storage.findAnnotationBodyWithTypeAndValue(type, value);
  }

  @Override
  public Optional<AlexandriaAnnotationBody> readAnnotationBody(UUID uuid) {
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
  public Optional<AlexandriaAnnotation> readAnnotation(UUID uuid) {
    return storage.readAnnotation(uuid);
  }

  @Override
  public Set<AlexandriaResource> readSubResources(UUID uuid) {
    return storage.readSubResources(uuid);
  }

  @Override
  public AlexandriaResource createSubResource(UUID uuid, UUID parentUuid, String sub, TentativeAlexandriaProvenance provenance) {
    AlexandriaResource subresource = new AlexandriaResource(uuid, provenance);
    subresource.setCargo(sub);
    subresource.setParentResourcePointer(new AccountablePointer<AlexandriaResource>(AlexandriaResource.class, parentUuid.toString()));
    storage.createSubResource(subresource);
    return subresource;
  }

  @Override
  public Optional<AlexandriaResource> readSubResource(UUID uuid) {
    return storage.readSubResource(uuid);
  }

  @Override
  public Optional<? extends Accountable> dereference(AccountablePointer<? extends Accountable> pointer) {
    Class<? extends Accountable> aClass = pointer.getAccountableClass();
    UUID uuid = UUID.fromString(pointer.getIdentifier());
    if (AlexandriaResource.class.equals(aClass)) {
      return readResource(uuid);

    } else if (AlexandriaAnnotation.class.equals(aClass)) {
      return readAnnotation(uuid);

    } else {
      throw new RuntimeException("unexpected accountableClass: " + aClass.getName());
    }
  }

}
