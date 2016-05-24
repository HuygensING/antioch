package nl.knaw.huygens.alexandria.client;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import nl.knaw.huygens.alexandria.api.model.Annotator;
import nl.knaw.huygens.alexandria.api.model.text.ResourceTextAnnotation;
import nl.knaw.huygens.alexandria.api.model.text.ResourceTextAnnotation.Position;
import nl.knaw.huygens.alexandria.api.model.text.ResourceTextAnnotationInfo;
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
    client.setAnnotator(resourceUuid, "ed", new Annotator().setCode("ed").setDescription("Eddy Wally"));

    UUID uuid = UUID.randomUUID();
    Position position = new Position()//
        .setXmlId("p-1")//
        .setOffset(6)//
        .setLength(2);
    ResourceTextAnnotation textAnnotation = new ResourceTextAnnotation()//
        .setId(uuid)//
        .setName("word")//
        .setAnnotator("ed")//
        .setPosition(position);
    RestResult<ResourceTextAnnotationInfo> putResult = client.setResourceTextAnnotation(resourceUuid, textAnnotation);
    assertRequestSucceeded(putResult);
    ResourceTextAnnotationInfo info = putResult.get();
    assertThat(info.getAnnotates()).isEqualTo("is");
  }

  @Test
  public void testSetResourceTextAnnotationWithInvalidXmlId() {
    String xml = singleQuotesToDouble("<text><p xml:id='p-1'>This is a simple paragraph.</p></text>");
    UUID resourceUuid = createResourceWithText(xml);
    client.setAnnotator(resourceUuid, "ed", new Annotator().setCode("ed").setDescription("Eddy Wally"));

    UUID uuid = UUID.randomUUID();
    Position position = new Position()//
        .setXmlId("undefined")//
        .setOffset(6)//
        .setLength(2);
    ResourceTextAnnotation textAnnotation = new ResourceTextAnnotation()//
        .setId(uuid)//
        .setName("word")//
        .setAnnotator("ed")//
        .setPosition(position);
    RestResult<ResourceTextAnnotationInfo> putResult = client.setResourceTextAnnotation(resourceUuid, textAnnotation);
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
