package nl.knaw.huygens.alexandria.client;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import nl.knaw.huygens.alexandria.api.model.TextEntity;
import nl.knaw.huygens.alexandria.api.model.TextImportStatus;

public class ResourceTextTest extends AlexandriaClientTest {
  @Before
  public void before() {
    client.setAuthKey(AUTHKEY);
    client.setAutoConfirm(true);
  }

  @Test
  public void testResourceText() {
    UUID resourceUuid = createResource("test");
    String xml = "<text>Something</text>";
    RestResult<URI> result = client.setResourceText(resourceUuid, xml);
    assertThat(result).isNotNull();
    assertThat(result.hasFailed()).isFalse();

    TextImportStatus textGraphImportStatus = null;
    boolean goOn = true;
    while (goOn) {
      RestResult<TextImportStatus> result2 = client.getTextImportStatus(resourceUuid);
      assertThat(result2.hasFailed()).isFalse();
      textGraphImportStatus = result2.get();
      goOn = !textGraphImportStatus.isDone();
    }
    URI expectedURI = URI.create("http://localhost:2016/resources/" + resourceUuid + "/text/xml");
    assertThat(textGraphImportStatus.getTextURI()).isEqualTo(expectedURI);

    RestResult<TextEntity> textInfoResult = client.getTextInfo(resourceUuid);
    assertRequestSucceeded(textInfoResult);
    TextEntity textEntity = textInfoResult.get();
    assertThat(textEntity.getTextViews()).isEmpty();
    assertThat(textEntity.getXmlURI()).isEqualTo(expectedURI);

    RestResult<String> xmlReadResult = client.getTextAsString(resourceUuid);
    assertRequestSucceeded(xmlReadResult);
    String xml2 = xmlReadResult.get();
    assertThat(xml2).isEqualTo(xml);

    RestResult<String> dotReadResult = client.getTextAsDot(resourceUuid);
    assertRequestSucceeded(dotReadResult);
    String dot = dotReadResult.get();
    String expectedDot = singleQuotesToDouble("digraph TextGraph {\n"//
        + "  ranksep=1.0\n"//
        + "  t0 [shape=box, label='Something'];\n"//
        + "  a0 [label='text'];\n"//
        + "  a0 -> t0 [color='blue'];\n"//
        + "  {rank=same;t0;}\n"//
        + "  {rank=same;a0;}\n"//
        + "}");
    assertThat(dot).isEqualTo(expectedDot);
  }

}
