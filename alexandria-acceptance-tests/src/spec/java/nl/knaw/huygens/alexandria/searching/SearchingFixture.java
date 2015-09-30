package nl.knaw.huygens.alexandria.searching;

import org.concordion.integration.junit4.ConcordionRunner;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;

import nl.knaw.huygens.alexandria.concordion.AlexandriaAcceptanceTest;
import nl.knaw.huygens.alexandria.endpoint.search.SearchEndpoint;

@RunWith(ConcordionRunner.class)
public class SearchingFixture extends AlexandriaAcceptanceTest {
  @BeforeClass
  public static void registerEndpoints() {
    register(SearchEndpoint.class);
  }
}
