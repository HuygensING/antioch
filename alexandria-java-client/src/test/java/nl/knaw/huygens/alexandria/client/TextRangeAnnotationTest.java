package nl.knaw.huygens.alexandria.client;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import nl.knaw.huygens.Log;
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

  @Test
  public void testSetTextRangeAnnotationOnElementWithoutText() {
    String xml = singleQuotesToDouble("<text><p xml:id='p-9'>Ex Musaeo,...</p>\n"//
        + "<p xml:id='p-10'>Tuus...</p>\n"//
        + "<p xml:id='p-11'>Prenez ...</p>\n"//
        + "<p xml:id='p-12'>Je vous ....</p>\n"//
        + "<p xml:id='p-13'>A <placeName key='se:saumur.fra'>Saumur</placeName>.</p>\n"//
        + "<p xml:id='p-14'><figure><graphic url='beec002jour04ill02.gif'/></figure></p></text>");
    UUID resourceUUID = createResourceWithText(xml);
    RestResult<Void> result = client.setAnnotator(resourceUUID, "ed", new Annotator().setCode("ed").setDescription("Eddy Thor"));
    assertRequestSucceeded(result);

    UUID uuid = UUID.randomUUID();
    Position position = new Position()//
        .setXmlId("p-14");
    TextRangeAnnotation textRangeAnnotation = new TextRangeAnnotation()//
        .setId(uuid)//
        .setName("hi")//
        .setAnnotator("ed")//
        .setPosition(position);
    RestResult<TextRangeAnnotationInfo> putResult = client.setResourceTextRangeAnnotation(resourceUUID, textRangeAnnotation);
    putResult.getFailureCause().ifPresent(c -> Log.info(c));
    assertThat(putResult.hasFailed()).isFalse();
    Log.info(putResult.get().toString());
  }

  @Test
  public void testSetNonOverlappingTextRangeAnnotations() {
    String xml = singleQuotesToDouble("<text><p xml:id='p-9'>Ex Musaeo,...</p>\n"//
        + "<p xml:id='p-10'>Tuus...</p>\n"//
        + "<p xml:id='p-11'>Prenez ...</p>\n"//
        + "<p xml:id='p-12'>Je vous ....</p>\n"//
        + "<p xml:id='p-13'>A <placeName key='se:saumur.fra'>Saumur</placeName>.</p>\n"//
        + "<p xml:id='p-14'><figure><graphic url='beec002jour04ill02.gif'/></figure></p></text>");
    UUID resourceUUID = createResourceWithText(xml);
    RestResult<Void> result = client.setAnnotator(resourceUUID, "ckcc", new Annotator().setCode("ckcc").setDescription("Cees Kacc"));
    assertRequestSucceeded(result);

    UUID closer13 = UUID.randomUUID();
    Position position = new Position()//
        .setXmlId("p-13");
    TextRangeAnnotation textRangeAnnotation = new TextRangeAnnotation()//
        .setId(closer13)//
        .setName("closer")//
        .setAnnotator("ckcc")//
        .setPosition(position);
    RestResult<TextRangeAnnotationInfo> putResult = client.setResourceTextRangeAnnotation(resourceUUID, textRangeAnnotation);
    putResult.getFailureCause().ifPresent(c -> Log.info(c));
    assertThat(putResult.hasFailed()).isFalse();
    Log.info(putResult.get().toString());

    UUID closer9 = UUID.randomUUID();
    Position position9 = new Position()//
        .setXmlId("p-9");
    TextRangeAnnotation textRangeAnnotation9 = new TextRangeAnnotation()//
        .setId(closer9)//
        .setName("closer")//
        .setAnnotator("ckcc")//
        .setPosition(position9);
    RestResult<TextRangeAnnotationInfo> putResult9 = client.setResourceTextRangeAnnotation(resourceUUID, textRangeAnnotation9);
    putResult9.getFailureCause().ifPresent(c -> Log.info(c));
    assertThat(putResult9.hasFailed()).isFalse();
    Log.info(putResult.get().toString());
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
