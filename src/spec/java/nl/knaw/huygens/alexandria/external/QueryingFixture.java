package nl.knaw.huygens.alexandria.external;

import nl.knaw.huygens.alexandria.RestFixture;
import org.concordion.api.ExpectedToFail;
import org.concordion.api.MultiValueResult;
import org.concordion.integration.junit4.ConcordionRunner;
import org.junit.runner.RunWith;

@ExpectedToFail
@RunWith(ConcordionRunner.class)
public class QueryingFixture extends RestFixture {
  public MultiValueResult testGET(final String method, final String path) {
    return invokeREST(method, path);
  }
}
