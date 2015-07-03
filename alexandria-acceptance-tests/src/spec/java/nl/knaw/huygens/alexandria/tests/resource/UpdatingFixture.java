package nl.knaw.huygens.alexandria.tests.resource;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.util.UUID;

import nl.knaw.huygens.alexandria.exception.NotFoundException;
import nl.knaw.huygens.alexandria.model.AlexandriaState;
import nl.knaw.huygens.alexandria.model.TentativeAlexandriaProvenance;
import org.concordion.api.ExpectedToFail;
import org.concordion.integration.junit4.ConcordionRunner;
import org.junit.runner.RunWith;

@ExpectedToFail
@RunWith(ConcordionRunner.class)
public class UpdatingFixture extends ResourcesBase {
  @Override
  public void request(String method, String path) {
    when(service().readResource(any(UUID.class))).thenThrow(new NotFoundException());
    when(service().createOrUpdateResource(any(UUID.class), any(String.class), any(TentativeAlexandriaProvenance.class), any(AlexandriaState.class))).thenReturn(false);

    super.request(method, path);
  }

}
