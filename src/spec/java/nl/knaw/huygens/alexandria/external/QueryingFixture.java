package nl.knaw.huygens.alexandria.external;

import static org.mockito.Mockito.doReturn;

import org.concordion.api.ExpectedToFail;
import org.concordion.integration.junit4.ConcordionRunner;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

@ExpectedToFail
@RunWith(ConcordionRunner.class)
public class QueryingFixture extends ResourcesFixture {
  @Override
  public void request(String method, String path) {

    doReturn("{ \"resource\":{\"body\": \"to be fixed\", \"createdOn\": \"2015-03-10 11:25:24+01:00\"} }")
        .when(resourceService()).getResource(Mockito.anyString());

    super.request(method, path);
  }
}
