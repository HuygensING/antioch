package nl.knaw.huygens.alexandria.external;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.concordion.api.ExpectedToFail;
import org.concordion.integration.junit4.ConcordionRunner;
import org.junit.runner.RunWith;

@ExpectedToFail
@RunWith(ConcordionRunner.class)
public class QueryingFixture extends ResourcesFixture {
  @Override
  public void request(String method, String path) {

    when(resourceService().getResource(any(UUID.class)))
        .thenReturn("{ \"resource\":{\"body\": \"to be fixed\", \"createdOn\": \"2015-03-10 11:25:24+01:00\"} }");

    super.request(method, path);
  }
}
