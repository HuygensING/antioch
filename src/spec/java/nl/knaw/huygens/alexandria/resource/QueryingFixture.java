package nl.knaw.huygens.alexandria.resource;

import static org.mockito.Mockito.when;

import java.util.UUID;

import nl.knaw.huygens.alexandria.exception.NotFoundException;
import nl.knaw.huygens.alexandria.model.AlexandriaAnnotation;
import nl.knaw.huygens.alexandria.model.AlexandriaResource;

public class QueryingFixture extends ResourceFixture {
  private AlexandriaResource resource;

  public void existingResource(String id) {
    UUID uuid = UUID.fromString(id);
    resource = new AlexandriaResource(uuid);
    when(resourceService().readResource(uuid)).thenReturn(resource);
  }

  public void withReference(String reference) {
    resource.setRef(reference);
  }

  public void withAnnotation(String id) {
    resource.addAnnotation(new AlexandriaAnnotation(UUID.fromString(id), "<type>", "<value>"));
  }

  public void noSuchResource(String id) {
    UUID uuid = UUID.fromString(id);
    when(resourceService().readResource(uuid)).thenThrow(new NotFoundException());
  }
}
