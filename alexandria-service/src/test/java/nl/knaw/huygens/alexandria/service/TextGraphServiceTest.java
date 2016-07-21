package nl.knaw.huygens.alexandria.service;

import static java.util.stream.Collectors.joining;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.UUID;

import javax.ws.rs.core.StreamingOutput;

import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;

import nl.knaw.huygens.Log;
import nl.knaw.huygens.alexandria.api.model.text.TextRangeAnnotation;
import nl.knaw.huygens.alexandria.api.model.text.TextRangeAnnotation.Position;
import nl.knaw.huygens.alexandria.config.MockConfiguration;
import nl.knaw.huygens.alexandria.endpoint.EndpointPathResolver;
import nl.knaw.huygens.alexandria.endpoint.LocationBuilder;
import nl.knaw.huygens.alexandria.model.AlexandriaResource;
import nl.knaw.huygens.alexandria.model.TentativeAlexandriaProvenance;
import nl.knaw.huygens.alexandria.storage.Storage;
import nl.knaw.huygens.alexandria.storage.frames.TextRangeAnnotationVF;
import nl.knaw.huygens.alexandria.test.AlexandriaTest;
import nl.knaw.huygens.alexandria.textgraph.DotFactory;
import nl.knaw.huygens.alexandria.textgraph.ParseResult;
import nl.knaw.huygens.alexandria.textgraph.TextAnnotation;
import nl.knaw.huygens.alexandria.textgraph.TextGraphUtil;

public class TextGraphServiceTest extends AlexandriaTest {

  private static final LocationBuilder LOCATION_BUILDER = new LocationBuilder(new MockConfiguration(), new EndpointPathResolver());
  private Storage storage = new Storage(TinkerGraph.open());
  private TinkerPopService service = new TinkerGraphService(LOCATION_BUILDER);
  private TextGraphService tgs;

  @Before
  public void before() {
    service.setStorage(storage);
    tgs = new TextGraphService(storage);
  }

  @Test
  public void testUpdateTextAnnotationLink1() {
    String xml = "<text><p xml:id=\"p-1\">This is a paragraph.</p></text>";
    String expected = "<text><p xml:id=\"p-1\">This <word resp=\"#ed\">is</word> a paragraph.</p></text>";
    assertAnnotationCorrectlyInserted(xml, expected);
  }

  @Test
  public void testUpdateTextAnnotationLink2() {
    String xml = "<text><p xml:id=\"p-1\">This <b>is a</b> different paragraph.</p></text>";
    String expected = "<text><p xml:id=\"p-1\">This <b><word resp=\"#ed\">is</word> a</b> different paragraph.</p></text>";
    assertAnnotationCorrectlyInserted(xml, expected);
  }

  @Test
  public void testUpdateTextAnnotationLink3() {
    String xml = "<text><p xml:id=\"p-1\"><i>This is</i> a third paragraph.</p></text>";
    String expected = "<text><p xml:id=\"p-1\"><i>This <word resp=\"#ed\">is</word></i> a third paragraph.</p></text>";
    assertAnnotationCorrectlyInserted(xml, expected);
  }

  @Test
  public void testUpdateTextAnnotationLink4() {
    String xml = "<text><p xml:id=\"p-1\">This <i>is</i> a test.</p></text>";
    String expected = "<text><p xml:id=\"p-1\"><lang value=\"?\" resp=\"#tool\">This <i>is</i> a test.</lang></p></text>";
    TextRangeAnnotation textRangeAnnotation = new TextRangeAnnotation()//
        .setId(UUID.randomUUID())//
        .setName("lang")//
        .setAnnotator("tool")//
        .setAttributes(ImmutableMap.of("value", "?"))//
        .setPosition(new Position().setXmlId("p-1").setOffset(1).setLength(15));

    UUID resourceUUID = UUID.randomUUID();
    Log.info("xml={}", xml);
    String xmlOut = storage.runInTransaction(() -> {
      createResourceWithText(resourceUUID, xml);
      TextRangeAnnotationVF vf = storage.createVF(TextRangeAnnotationVF.class);
      dumpDot(resourceUUID);
      tgs.updateTextAnnotationLink(vf, textRangeAnnotation, resourceUUID);
      dumpDot(resourceUUID);
      return getXML(resourceUUID);
    });

    softly.assertThat(xmlOut).isEqualTo(expected);
  }

  @Test
  public void testUpdateTextAnnotationLink5() {
    String xml = "<text xml:id=\"text-1\"><p xml:id=\"p-1\"><i>This</i> is a test.</p></text>";
    String expected = "<text xml:id=\"text-1\"><lang value=\"?\" resp=\"#tool\"><p xml:id=\"p-1\"><i>This</i> is a test.</p></lang></text>";
    Position position = new Position().setXmlId("text-1").setOffset(1).setLength(15);
    ImmutableMap<String, String> attributes = ImmutableMap.of("value", "?");
    UUID resourceUUID = UUID.randomUUID();
    TextRangeAnnotation textRangeAnnotation = new TextRangeAnnotation()//
        .setId(resourceUUID)//
        .setAnnotator("tool")//
        .setName("lang")//
        .setAttributes(attributes)//
        .setUseOffset(false)//
        .setPosition(position);

    assertExpectation(xml, expected, resourceUUID, textRangeAnnotation);
  }

  @Test
  public void testUpdateTextAnnotationLink6() {
    String xml = "<text xml:id=\"text-1\"><p xml:id=\"p-1\"><i>This</i> is encroyable.</p></text>";
    String expected = "<text xml:id=\"text-1\"><p xml:id=\"p-1\"><lang value=\"en\" resp=\"#tool\"><i>This</i> is</lang> encroyable.</p></text>";
    Position position = new Position().setXmlId("text-1").setOffset(1).setLength(7);
    ImmutableMap<String, String> attributes = ImmutableMap.of("value", "en");
    UUID resourceUUID = UUID.randomUUID();
    TextRangeAnnotation textRangeAnnotation = new TextRangeAnnotation()//
        .setId(resourceUUID)//
        .setAnnotator("tool")//
        .setName("lang")//
        .setAttributes(attributes)//
        .setPosition(position);

    assertExpectation(xml, expected, resourceUUID, textRangeAnnotation);
  }

  private void assertExpectation(String xml, String expected, UUID resourceUUID, TextRangeAnnotation textRangeAnnotation) {
    Log.info("xml={}", xml);
    String xmlOut = storage.runInTransaction(() -> {
      createResourceWithText(resourceUUID, xml);
      showTextAnnotationList(resourceUUID);
      TextRangeAnnotationVF vf = storage.createVF(TextRangeAnnotationVF.class);
      // dumpDot(resourceUUID);
      tgs.updateTextAnnotationLink(vf, textRangeAnnotation, resourceUUID);
      // dumpDot(resourceUUID);
      showTextAnnotationList(resourceUUID);
      return getXML(resourceUUID);
    });

    softly.assertThat(xmlOut).isEqualTo(expected);
  }

  private void showTextAnnotationList(UUID resourceUUID) {
    TextGraphService tgs = new TextGraphService(storage);

    String textAnnotations = tgs.getTextAnnotationStream(resourceUUID)//
        .map(this::logTextAnnotation)//
        .collect(joining());
    Log.info("TextAnnotations = {}", textAnnotations);
  }

  private String logTextAnnotation(TextAnnotation ta) {
    String string = "<" + ta.getName() + " depth=" + ta.getDepth()
        + ta.getAttributes()//
            .entrySet()//
            .stream()//
            .map(kv -> " " + kv.getKey() + "='" + kv.getValue() + "'")//
            .collect(joining(" "))
        + ">";
    Log.info(string);
    return string;
  }

  private void assertAnnotationCorrectlyInserted(String xml, String expected) {
    String xmlOut = getAnnotatedText(xml);
    softly.assertThat(xmlOut).isEqualTo(expected);
  }

  private String getAnnotatedText(String xml) {
    TextRangeAnnotation textRangeAnnotation = new TextRangeAnnotation()//
        .setId(UUID.randomUUID())//
        .setName("word")//
        .setAnnotator("ed")//
        .setPosition(new Position().setXmlId("p-1").setOffset(6).setLength(2));
    UUID resourceUUID = UUID.randomUUID();
    Log.info("xml={}", xml);
    return storage.runInTransaction(() -> {
      createResourceWithText(resourceUUID, xml);
      TextRangeAnnotationVF vf = storage.createVF(TextRangeAnnotationVF.class);

      // dumpDot(resourceUUID);
      tgs.updateTextAnnotationLink(vf, textRangeAnnotation, resourceUUID);
      dumpDot(resourceUUID);
      return getXML(resourceUUID);
    });
  }

  private String getXML(UUID resourceUUID) {
    StreamingOutput outputStream = TextGraphUtil.streamXML(service, resourceUUID);
    return TextGraphUtil.asString(outputStream);
  }

  private void createResourceWithText(UUID resourceUUID, String xml) {
    TentativeAlexandriaProvenance provenance = new TentativeAlexandriaProvenance("who", Instant.now(), "why");
    AlexandriaResource resource = new AlexandriaResource(resourceUUID, provenance);
    service.createOrUpdateResource(resource);
    ParseResult parseresult = TextGraphUtil.parse(xml);
    service.storeTextGraph(resourceUUID, parseresult);
  }

  void dumpDb() {
    Log.info("dumping server graph as graphML:");
    try {
      ByteArrayOutputStream outputstream = new ByteArrayOutputStream();
      service.dumpToGraphML(outputstream);
      outputstream.flush();
      outputstream.close();
      System.out.println(outputstream.toString());
    } catch (IOException e) {
      e.printStackTrace();
    }
    Log.info("dumping done");
  }

  private void dumpDot(UUID resourceUUID) {
    Log.info("dumping textgraph as dot:");
    String dot = DotFactory.createDot(service, resourceUUID);
    Log.info("dot={}", dot);
    Log.info("dumping done");
  }

}
