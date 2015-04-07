package nl.knaw.huygens.alexandria.resource;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.util.UUID;

import nl.knaw.huygens.alexandria.model.AlexandriaResource;

public class CreationFixture extends ResourceFixture {
  @Override
  public void request(String method, String path) {
    final AlexandriaResource mockResource = new AlexandriaResource(UUID.randomUUID());
    when(resourceService().createResource(any(UUID.class))).thenReturn(mockResource);

    super.request(method, path);
  }

}
