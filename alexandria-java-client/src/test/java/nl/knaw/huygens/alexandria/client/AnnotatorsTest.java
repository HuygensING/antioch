package nl.knaw.huygens.alexandria.client;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import nl.knaw.huygens.alexandria.api.model.Annotator;
import nl.knaw.huygens.alexandria.api.model.AnnotatorList;

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
    setResourceAnnotator(resourceUuid, code, description);

    RestResult<Annotator> getResult = client.getAnnotator(resourceUuid, code);
    assertRequestSucceeded(getResult);
    Annotator annotator2 = getResult.get();
    assertThat(annotator2.getCode()).isEqualTo(code);
    assertThat(annotator2.getDescription()).isEqualTo(description);
    assertThat(annotator2.getResourceURI()).hasToString("http://localhost:2016/resources/" + resourceUuid);
  }

  private void setResourceAnnotator(UUID resourceUuid, String code, String description) {
    Annotator annotator = new Annotator().setDescription(description);
    RestResult<URI> putResult = client.setAnnotator(resourceUuid, code, annotator);
    assertRequestSucceeded(putResult);
    URI uri = putResult.get();
    assertThat(uri).hasToString("http://localhost:2016/resources/" + resourceUuid + "/annotators/abc");
  }

  @Test
  public void testAnnotators() {
    String resourceRef = "test";
    UUID resourceUUID = createResource(resourceRef);
    AnnotatorList annotatorList = getAnnotatorList(resourceUUID);
    assertThat(annotatorList).isEmpty();

    UUID subresourceUUID = createSubResource(resourceUUID, "ref");
    String code = "abc";
    String description = "Annotator abc";
    setResourceAnnotator(resourceUUID, code, description);
    AnnotatorList annotatorList2 = getAnnotatorList(subresourceUUID);
    assertThat(annotatorList2).hasSize(1);
    Annotator annotator = annotatorList2.get(0);
    assertThat(annotator.getCode()).isEqualTo(code);
    assertThat(annotator.getDescription()).isEqualTo(description);
  }

  private AnnotatorList getAnnotatorList(UUID resourceUUID) {
    RestResult<AnnotatorList> result = client.getAnnotators(resourceUUID);
    assertRequestSucceeded(result);
    AnnotatorList annotatorList = result.get();
    return annotatorList;
  }

}
