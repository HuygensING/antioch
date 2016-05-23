package nl.knaw.huygens.alexandria.client;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import nl.knaw.huygens.alexandria.api.model.text.ResourceTextAnnotation;
import nl.knaw.huygens.alexandria.api.model.text.ResourceTextAnnotation.Element;
import nl.knaw.huygens.alexandria.api.model.text.ResourceTextAnnotation.Position;
import nl.knaw.huygens.alexandria.api.model.text.TextImportStatus;

public class ResourceTextAnnotationTest extends AlexandriaClientTest {
  @Before
  public void before() {
    client.setAuthKey(AUTHKEY);
  }

  @Test
  public void testSetResourceTextAnnotation() {
    String xml = singleQuotesToDouble("<text><p xml:id='p-1'>This is a simple paragraph.</p></text>");
    UUID resourceUuid = createResourceWithText(xml);

    UUID uuid = UUID.randomUUID();
    Position position = new Position()//
        .setXmlId("p-1")//
        .setLength(2)//
        .setOffset(5);
    Element element = new Element()//
        .setName("word")//
        .setRespValue("#ed");
    ResourceTextAnnotation textAnnotation = new ResourceTextAnnotation()//
        .setId(uuid)//
        .setPosition(position)//
        .setElement(element);
    RestResult<URI> putResult = client.setResourceTextAnnotation(resourceUuid, textAnnotation);
    assertRequestSucceeded(putResult);
  }

  @Test
  public void testSetResourceTextAnnotationWithInvalidXmlId() {
    String xml = singleQuotesToDouble("<text><p xml:id='p-1'>This is a simple paragraph.</p></text>");
    UUID resourceUuid = createResourceWithText(xml);

    UUID uuid = UUID.randomUUID();
    Position position = new Position()//
        .setXmlId("undefined")//
        .setLength(2)//
        .setOffset(5);
    Element element = new Element()//
        .setName("word")//
        .setRespValue("#ed");
    ResourceTextAnnotation textAnnotation = new ResourceTextAnnotation()//
        .setId(uuid)//
        .setPosition(position)//
        .setElement(element);
    RestResult<URI> putResult = client.setResourceTextAnnotation(resourceUuid, textAnnotation);
    assertThat(putResult.hasFailed()).isTrue();
    assertThat(putResult.getErrorMessage().get()).isEqualTo("The text does not contain an element with the specified xml:id.");
  }

  private UUID createResourceWithText(String xml) {
    String resourceRef = "test";
    UUID resourceUuid = createResource(resourceRef);
    TextImportStatus textGraphImportStatus = setResourceText(resourceUuid, xml);
    URI expectedURI = URI.create("http://localhost:2016/resources/" + resourceUuid + "/text/xml");
    assertThat(textGraphImportStatus.getTextURI()).isEqualTo(expectedURI);
    return resourceUuid;
  }

}
