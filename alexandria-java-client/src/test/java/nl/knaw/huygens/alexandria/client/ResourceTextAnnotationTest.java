package nl.knaw.huygens.alexandria.client;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import nl.knaw.huygens.alexandria.api.model.Annotator;
import nl.knaw.huygens.alexandria.api.model.text.TextRangeAnnotation;
import nl.knaw.huygens.alexandria.api.model.text.TextRangeAnnotation.Position;
import nl.knaw.huygens.alexandria.api.model.text.TextRangeAnnotationInfo;
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
    RestResult<URI> result = client.setAnnotator(resourceUuid, "ed", new Annotator().setCode("ed").setDescription("Eddy Wally"));
    assertRequestSucceeded(result);

    UUID uuid = UUID.randomUUID();
    Position position = new Position()//
        .setXmlId("p-1")//
        .setOffset(6)//
        .setLength(2);
    TextRangeAnnotation textAnnotation = new TextRangeAnnotation()//
        .setId(uuid)//
        .setName("word")//
        .setAnnotator("ed")//
        .setPosition(position);
    RestResult<TextRangeAnnotationInfo> putResult = client.setResourceTextAnnotation(resourceUuid, textAnnotation);
    assertRequestSucceeded(putResult);
    TextRangeAnnotationInfo info = putResult.get();
    assertThat(info.getAnnotates()).isEqualTo("is");
  }

  @Test
  public void testSetResourceTextAnnotationWithInvalidXmlId() {
    String xml = singleQuotesToDouble("<text><p xml:id='p-1'>This is a simple paragraph.</p></text>");
    UUID resourceUuid = createResourceWithText(xml);
    RestResult<URI> result = client.setAnnotator(resourceUuid, "ed", new Annotator().setCode("ed").setDescription("Eddy Wally"));
    assertRequestSucceeded(result);

    UUID uuid = UUID.randomUUID();
    Position position = new Position()//
        .setXmlId("undefined")//
        .setOffset(6)//
        .setLength(2);
    TextRangeAnnotation textAnnotation = new TextRangeAnnotation()//
        .setId(uuid)//
        .setName("word")//
        .setAnnotator("ed")//
        .setPosition(position);
    RestResult<TextRangeAnnotationInfo> putResult = client.setResourceTextAnnotation(resourceUuid, textAnnotation);
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
