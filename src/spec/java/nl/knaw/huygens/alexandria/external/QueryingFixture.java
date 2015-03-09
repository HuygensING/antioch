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

    doReturn("{ \"resource\":{\"body\": \"to be fixed\"} }").when(resourceService()).getResource(Mockito.anyString());

    super.request(method, path);

    json().ifPresent((json) -> System.err.println("JSON: " + json.findPath("body")));
  }
}
