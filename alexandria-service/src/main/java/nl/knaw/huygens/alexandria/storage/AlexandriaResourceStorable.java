package nl.knaw.huygens.alexandria.storage;

import java.util.Set;
import java.util.UUID;
import nl.knaw.huygens.alexandria.model.AlexandriaAnnotation;
import nl.knaw.huygens.alexandria.model.AlexandriaProvenance;
import nl.knaw.huygens.alexandria.model.AlexandriaResource;

public class AlexandriaResourceStorable implements Storable<AlexandriaResource> {

  private AlexandriaResource delegate;

  @Override
  public AlexandriaResource get() {
    return delegate;
  }

  public static Storable<AlexandriaResource> of(AlexandriaResource resource) {
    AlexandriaResourceStorable alexandriaResource = new AlexandriaResourceStorable();
    alexandriaResource.delegate = resource;
    return alexandriaResource;
  }

  public Set<AlexandriaAnnotation> getAnnotations() {
    return delegate.getAnnotations();
  }

  public UUID getId() {
    return delegate.getId();
  }

  public void addAnnotation(AlexandriaAnnotation annotation) {
    delegate.addAnnotation(annotation);
  }

  public String getRef() {
    return delegate.getRef();
  }

  public void setRef(String ref) {
    delegate.setRef(ref);
  }

  public AlexandriaProvenance getProvenance() {
    return delegate.getProvenance();
  }

}
