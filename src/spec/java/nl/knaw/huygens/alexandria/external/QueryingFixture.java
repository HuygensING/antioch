package nl.knaw.huygens.alexandria.external;

import static org.mockito.Mockito.when;

import java.util.UUID;

import nl.knaw.huygens.alexandria.exception.NotFoundException;
import nl.knaw.huygens.alexandria.model.AlexandriaResource;
import org.concordion.integration.junit4.ConcordionRunner;
import org.junit.runner.RunWith;

//@ExpectedToFail
@RunWith(ConcordionRunner.class)
public class QueryingFixture extends ResourcesFixture {
  @Override
  public void request(String method, String path) {

    when(resourceService().readResource(UUID.fromString("3ed4faaa-c0cd-11e4-a84e-83ef41cbdc44")))
        .thenThrow(new NotFoundException());

    when(resourceService().readResource(UUID.fromString("c6b96360-c0c9-11e4-b947-6bc57448d166")))
        .thenReturn(new AlexandriaResource(UUID.fromString("c6b96360-c0c9-11e4-b947-6bc57448d166")));

    super.request(method, path);
  }
}
