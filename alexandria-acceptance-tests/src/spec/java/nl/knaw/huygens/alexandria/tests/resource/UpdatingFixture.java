package nl.knaw.huygens.alexandria.tests.resource;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.concordion.api.ExpectedToFail;

import nl.knaw.huygens.alexandria.exception.NotFoundException;
import nl.knaw.huygens.alexandria.model.AlexandriaState;
import nl.knaw.huygens.alexandria.model.TentativeAlexandriaProvenance;

@ExpectedToFail
public class UpdatingFixture extends ResourceFixture {
  @Override
  public void request(String method, String path) {
    when(service().readResource(any(UUID.class))).thenThrow(new NotFoundException());
    when(service().createOrUpdateResource(any(UUID.class), any(String.class), any(TentativeAlexandriaProvenance.class), any(AlexandriaState.class))).thenReturn(false);

    super.request(method, path);
  }

}
