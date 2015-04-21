package nl.knaw.huygens.alexandria.resource;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.util.UUID;

import nl.knaw.huygens.alexandria.exception.NotFoundException;
import nl.knaw.huygens.alexandria.model.AlexandriaResource;
import org.concordion.api.ExpectedToFail;

@ExpectedToFail
public class CreationFixture extends ResourceFixture {
  @Override
  public void request(String method, String path) {
    when(resourceService().readResource(any(UUID.class))).thenThrow(new NotFoundException());

    final AlexandriaResource mockResource = new AlexandriaResource(UUID.randomUUID());
    when(resourceService().createResource(any(UUID.class))).thenReturn(mockResource);

    super.request(method, path);
  }

}
