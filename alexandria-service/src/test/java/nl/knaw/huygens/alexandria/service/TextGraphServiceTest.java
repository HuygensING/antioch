package nl.knaw.huygens.alexandria.service;

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
import nl.knaw.huygens.alexandria.textgraph.TextGraphUtil;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.StreamingOutput;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.UUID;

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

  private void assertAnnotationCorrectlyInserted(String xml, String expected) {
    String xmlOut = getAnnotatedText(xml);
    softly.assertThat(xmlOut).isEqualTo(expected);
  }

  private String getAnnotatedText(String xml) {
    TextRangeAnnotation textRangeAnnotation = new TextRangeAnnotation()
      .setId(UUID.randomUUID())

      .setName("word")
      .setAnnotator("ed")
      .setPosition(new Position()
        .setXmlId("p-1")
        .setOffset(6)
        .setLength(2));
    UUID resourceUUID = UUID.randomUUID();
    Log.info("xml={}", xml);
    return storage.runInTransaction(() -> {
      createResourceWithText(resourceUUID, xml);
      TextRangeAnnotationVF vf = storage.createVF(TextRangeAnnotationVF.class);

//      dumpDot(resourceUUID);
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
