package nl.knaw.huygens.alexandria.storage;

import java.util.UUID;
import nl.knaw.huygens.alexandria.model.AlexandriaResource;

public class Storage {

  public boolean exists(Class clazz, UUID uuid) {
    return false;
  }

  public AlexandriaResource read(Class clazz, UUID uuid) {
    if (clazz.equals(AlexandriaResource.class)) {
      Storable<AlexandriaResource> sar = new AlexandriaResourceStorable();
    }
    return null;
  }

  public void createOrUpdate(AlexandriaResource resource) {
    Storable<AlexandriaResource> sar = AlexandriaResourceStorable.of(resource);

  }

}
