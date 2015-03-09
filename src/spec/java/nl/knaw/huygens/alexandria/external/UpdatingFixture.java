package nl.knaw.huygens.alexandria.external;

import org.concordion.api.ExpectedToFail;
import org.concordion.api.MultiValueResult;
import org.concordion.integration.junit4.ConcordionRunner;
import org.junit.runner.RunWith;

@ExpectedToFail
@RunWith(ConcordionRunner.class)
public class UpdatingFixture extends ResourcesFixture {
  public MultiValueResult rest(String method, String path) {
    return invokeREST(method, path);
  }
}
