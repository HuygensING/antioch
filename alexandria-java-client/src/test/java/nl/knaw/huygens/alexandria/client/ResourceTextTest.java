package nl.knaw.huygens.alexandria.client;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import nl.knaw.huygens.alexandria.api.model.text.TextEntity;
import nl.knaw.huygens.alexandria.api.model.text.TextImportStatus;

public class ResourceTextTest extends AlexandriaClientTest {
  @Before
  public void before() {
    client.setAuthKey(AUTHKEY);
    client.setAutoConfirm(true);
  }

  @Test
  public void testResourceText() {
    String resourceRef = "test";
    UUID resourceUuid = createResource(resourceRef);
    String xml = "<text>Something</text>";
    TextImportStatus textGraphImportStatus = setResourceText(resourceUuid, xml);
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

  @Test
  public void testMilestoneHandling() {
    UUID resourceUuid = createResource("test");
    String xml = singleQuotesToDouble("<text><pb n='1' xml:id='pb-1'/><p><figure><graphic url='beec002jour04ill02.gif'/></figure></p></text>");
    TextImportStatus textGraphImportStatus = setResourceText(resourceUuid, xml);
    URI expectedURI = URI.create("http://localhost:2016/resources/" + resourceUuid + "/text/xml");
    assertThat(textGraphImportStatus.getTextURI()).isEqualTo(expectedURI);

    RestResult<String> xmlReadResult = client.getTextAsString(resourceUuid);
    assertRequestSucceeded(xmlReadResult);
    String xml2 = xmlReadResult.get();
    assertThat(xml2).isEqualTo(xml);
  }
}
