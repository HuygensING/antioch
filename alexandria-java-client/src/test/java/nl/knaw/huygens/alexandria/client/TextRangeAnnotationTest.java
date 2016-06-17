package nl.knaw.huygens.alexandria.client;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import nl.knaw.huygens.alexandria.api.model.Annotator;
import nl.knaw.huygens.alexandria.api.model.text.TextImportStatus;
import nl.knaw.huygens.alexandria.api.model.text.TextRangeAnnotation;
import nl.knaw.huygens.alexandria.api.model.text.TextRangeAnnotation.Position;
import nl.knaw.huygens.alexandria.api.model.text.TextRangeAnnotationInfo;

public class TextRangeAnnotationTest extends AlexandriaClientTest {
  @Before
  public void before() {
    client.setAuthKey(AUTHKEY);
  }

  @Test
  public void testSetTextRangeAnnotation() {
    String xml = singleQuotesToDouble("<text><p xml:id='p-1'>This is a simple paragraph.</p></text>");
    UUID resourceUUID = createResourceWithText(xml);
    RestResult<Void> result = client.setAnnotator(resourceUUID, "ed", new Annotator().setCode("ed").setDescription("Eddy Wally"));
    assertRequestSucceeded(result);

    UUID annotationUUID = UUID.randomUUID();
    Position position = new Position()//
        .setXmlId("p-1")//
        .setOffset(6)//
        .setLength(2);
    TextRangeAnnotation textRangeAnnotation0 = new TextRangeAnnotation()//
        .setId(annotationUUID)//
        .setName("word")//
        .setAnnotator("ed")//
        .setPosition(position);
    RestResult<TextRangeAnnotationInfo> putResult = client.setResourceTextRangeAnnotation(resourceUUID, textRangeAnnotation0);
    assertRequestSucceeded(putResult);
    TextRangeAnnotationInfo info = putResult.get();
    assertThat(info.getAnnotates()).isEqualTo("is");
    dumpDb();

    RestResult<TextRangeAnnotation> getResult = client.getResourceTextRangeAnnotation(resourceUUID, annotationUUID);
    assertRequestSucceeded(getResult);
    TextRangeAnnotation textRangeAnnotation1 = getResult.get();
    assertThat(textRangeAnnotation1).isEqualToComparingFieldByFieldRecursively(textRangeAnnotation0);
  }

  @Test
  public void testSetTextRangeAnnotationWithInvalidXmlId() {
    String xml = singleQuotesToDouble("<text><p xml:id='p-1'>This is a simple paragraph.</p></text>");
    UUID resourceUUID = createResourceWithText(xml);
    RestResult<Void> result = client.setAnnotator(resourceUUID, "ed", new Annotator().setCode("ed").setDescription("Eddy Wally"));
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
    RestResult<TextRangeAnnotationInfo> putResult = client.setResourceTextRangeAnnotation(resourceUUID, textAnnotation);
    assertThat(putResult.hasFailed()).isTrue();
    assertThat(putResult.getErrorMessage().get()).isEqualTo("The text does not contain an element with the specified xml:id.");
  }

  private UUID createResourceWithText(String xml) {
    String resourceRef = "test";
    UUID resourceUUID = createResource(resourceRef);
    TextImportStatus textGraphImportStatus = setResourceText(resourceUUID, xml);
    URI expectedURI = URI.create("http://localhost:2016/resources/" + resourceUUID + "/text/xml");
    assertThat(textGraphImportStatus.getTextURI()).isEqualTo(expectedURI);
    return resourceUUID;
  }

}
