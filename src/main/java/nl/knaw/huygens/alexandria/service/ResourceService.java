package nl.knaw.huygens.alexandria.service;

import javax.ws.rs.core.Context;
import java.util.UUID;

import nl.knaw.huygens.alexandria.external.IllegalResourceException;
import nl.knaw.huygens.alexandria.external.ResourceExistsException;
import nl.knaw.huygens.alexandria.external.ResourceStore;
import nl.knaw.huygens.alexandria.util.UUIDParser;

public class ResourceService {
  private final ResourceStore resourceStore;

  public ResourceService(@Context ResourceStore resourceStore) {
    this.resourceStore = resourceStore;
  }

  public UUID createResource(final String ref) {
    final UUID uuid = UUID.randomUUID();
    resourceStore.createResource(uuid, ref);
    return uuid;
  }

  public void createResource(final UUID uuid, final String ref) throws ResourceExistsException {
    if (!resourceStore.createResource(uuid, ref)) {
      throw new ResourceExistsException();
    }
  }

  public void createResource(final String id, final String ref)
      throws IllegalResourceException, ResourceExistsException {
    UUID uuid = UUIDParser.fromString(id).get().orElseThrow(IllegalResourceException::new);
    createResource(uuid, ref);
  }

  public String getResource(String id) throws IllegalResourceException {
    UUID uuid = UUIDParser.fromString(id).get().orElseThrow(IllegalResourceException::new);
    return resourceStore.getReference(uuid);
  }
}
