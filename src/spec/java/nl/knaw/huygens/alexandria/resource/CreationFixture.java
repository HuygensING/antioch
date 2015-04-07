package nl.knaw.huygens.alexandria.resource;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.util.UUID;

import nl.knaw.huygens.alexandria.model.AlexandriaResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CreationFixture extends ResourceFixture {
  private static final Logger LOG = LoggerFactory.getLogger(CreationFixture.class);

  @Override
  public void request(String method, String path) {
    LOG.trace("[{}] request to [{}]", method, path);

    final AlexandriaResource mockResource = new AlexandriaResource(UUID.randomUUID());
    when(resourceService().createResource(any(UUID.class))).thenReturn(mockResource);

    super.request(method, path);
  }

}
