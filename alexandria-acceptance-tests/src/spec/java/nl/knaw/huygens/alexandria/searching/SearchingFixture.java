package nl.knaw.huygens.alexandria.searching;

import static java.util.UUID.randomUUID;

import java.util.UUID;

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

  public void setupPagingStorage(String num) {
    clearStorage();

    for (int i = 0; i < Integer.valueOf(num); i++) {
      generateAnnotatedResource(randomUUID());
    }
  }

  private void generateAnnotatedResource(UUID uuid) {
    resourceExists(uuid.toString());
    hasConfirmedAnnotation(theResource(uuid), anAnnotation());
  }

}
