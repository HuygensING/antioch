package nl.knaw.huygens.alexandria.external;

import org.concordion.api.ExpectedToFail;
import org.concordion.api.MultiValueResult;
import org.concordion.integration.junit4.ConcordionRunner;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

@ExpectedToFail
@RunWith(ConcordionRunner.class)
public class QueryingFixture extends ResourcesFixture {
  public MultiValueResult rest(String method, String path) throws IllegalReferenceException {

    Mockito.doReturn("{ \"resource\":{\"body\": \"to be fixed\"} }").when(referenceService())
           .getReference(Mockito.anyString());

    final MultiValueResult result = invokeREST(method, path);

    json().ifPresent((json) -> System.err.println("JSON: " + json.findPath("body")));

    return result;
  }
}
