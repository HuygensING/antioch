package nl.knaw.huygens.alexandria.client;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import nl.knaw.huygens.alexandria.api.model.Annotator;

public class AnnotatorsTest extends AlexandriaClientTest {
  @Before
  public void before() {
    client.setAuthKey(AUTHKEY);
    client.setAutoConfirm(true);
  }

  @Test
  public void testSetThenGetAnnotator() {
    String resourceRef = "test";
    UUID resourceUuid = createResource(resourceRef);
    String code = "abc";
    String description = "Annotator description";
    Annotator annotator = new Annotator().setDescription(description);
    RestResult<URI> putResult = client.setAnnotator(resourceUuid, code, annotator);
    assertRequestSucceeded(putResult);
    URI uri = putResult.get();
    assertThat(uri).hasToString("http://localhost:2016/resources/" + resourceUuid + "/annotators/abc");

    RestResult<Annotator> getResult = client.getAnnotator(resourceUuid, code);
    assertRequestSucceeded(getResult);
    Annotator annotator2 = getResult.get();
    assertThat(annotator2.getCode()).isEqualTo(code);
    assertThat(annotator2.getDescription()).isEqualTo(description);
    assertThat(annotator2.getResourceURI()).hasToString("http://localhost:2016/resources/" + resourceUuid);
  }

}
