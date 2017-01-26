package nl.knaw.huygens.alexandria.service;

import static java.util.stream.Collectors.joining;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.ws.rs.core.StreamingOutput;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import nl.knaw.huygens.Log;
import nl.knaw.huygens.alexandria.api.model.text.TextRangeAnnotation;
import nl.knaw.huygens.alexandria.api.model.text.TextRangeAnnotation.AbsolutePosition;
import nl.knaw.huygens.alexandria.api.model.text.TextRangeAnnotation.Position;
import nl.knaw.huygens.alexandria.config.MockConfiguration;
import nl.knaw.huygens.alexandria.endpoint.EndpointPathResolver;
import nl.knaw.huygens.alexandria.endpoint.LocationBuilder;
import nl.knaw.huygens.alexandria.model.AlexandriaResource;
import nl.knaw.huygens.alexandria.model.TentativeAlexandriaProvenance;
import nl.knaw.huygens.alexandria.storage.EdgeLabels;
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
    setAbsolutePosition(textRangeAnnotation);
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

  @Test
  public void testUpdateTextAnnotationLinkNLA318() {
    String xml = //
        singleQuotesToDouble("<p xml:id='p-1'>...epouse mad<sup>le</sup> "//
            + "de <sic>Gendrin</sic>"//
            + " soeur du feu archevesque de Sens...</p>");
    String expected = //
        singleQuotesToDouble("<p xml:id='p-1'>...epouse mad<sup>le</sup> "//
            + "<persName key='S0328208' resp='#ckcc'>de <sic>Gendrin</sic></persName>"//
            + " soeur du feu archevesque de Sens...</p>");
    Position position1 = new Position()//
        .setXmlId("p-1")//
        .setOffset(17)//
        .setLength(10);
    Map<String, String> attributes = ImmutableMap.of("key", "S0328208");
    UUID annotationUUID = UUID.randomUUID();
    TextRangeAnnotation textRangeAnnotation = new TextRangeAnnotation()//
        .setId(annotationUUID)//
        .setName("persName")//
        .setAnnotator("ckcc")//
        .setPosition(position1)//
        .setAttributes(attributes);

    assertExpectation(xml, expected, annotationUUID, textRangeAnnotation);
  }

  @Test
  public void testUpdateTextAnnotationLinkNLA318a() {
    String xml = //
        singleQuotesToDouble("<ae xml:id='a-1'>A <be>B <c>C</c> D <e>E</e></be></ae>");
    String expected = //
        singleQuotesToDouble("<ae xml:id='a-1'>A <be>B <cd resp='#ckcc'><c>C</c> D</cd> <e>E</e></be></ae>");
    Position position1 = new Position()//
        .setXmlId("a-1")//
        .setOffset(5)//
        .setLength(3);
    UUID annotationUUID = UUID.randomUUID();
    TextRangeAnnotation textRangeAnnotation = new TextRangeAnnotation()//
        .setId(annotationUUID)//
        .setName("cd")//
        .setAnnotator("ckcc")//
        .setPosition(position1);

    assertExpectation(xml, expected, annotationUUID, textRangeAnnotation);
  }

  @Test
  public void testUpdateTextAnnotationLinkbNLA318a() {
    String xml = //
        singleQuotesToDouble("<ae xml:id='a-1'>A <be>B <c>C</c> D <e>E</e></be></ae>");
    String expected = //
        singleQuotesToDouble("<ae xml:id='a-1'>A <be><bd resp='#ckcc'>B <c>C</c> D</bd> <e>E</e></be></ae>");
    Position position1 = new Position()//
        .setXmlId("a-1")//
        .setOffset(3)//
        .setLength(5);
    UUID annotationUUID = UUID.randomUUID();
    TextRangeAnnotation textRangeAnnotation = new TextRangeAnnotation()//
        .setId(annotationUUID)//
        .setName("bd")//
        .setAnnotator("ckcc")//
        .setPosition(position1);

    assertExpectation(xml, expected, annotationUUID, textRangeAnnotation);
  }

  @Test
  public void testCreatePairsWithTwoLayers() throws Exception {
    Vertex v1 = mockVertex("1");
    Vertex v2 = mockVertex("2");
    Vertex va = mockVertex("a");
    Vertex vb = mockVertex("b");
    List<Vertex> layer1 = ImmutableList.of(v1, v2);
    List<Vertex> layer2 = ImmutableList.of(va, vb);
    List<List<Vertex>> vertexListPerLayer = ImmutableList.of(layer1, layer2);

    List<Pair<Vertex, Vertex>> pairs = tgs.createPairs(vertexListPerLayer);
    Log.info("pairs={}", pairs);
    assertThat(pairs).containsExactly(//
        Pair.of(v1, va), //
        Pair.of(v1, vb), //
        Pair.of(v2, va), //
        Pair.of(v2, vb)//
    );
  }

  @Test
  public void testCreatePairsWithThreeLayers() throws Exception {
    Vertex v1 = mockVertex("1");
    Vertex v2 = mockVertex("2");
    Vertex va = mockVertex("a");
    Vertex vb = mockVertex("b");
    Vertex vx = mockVertex("X");
    Vertex vy = mockVertex("Y");
    List<Vertex> layer1 = ImmutableList.of(v1, v2);
    List<Vertex> layer2 = ImmutableList.of(va, vb);
    List<Vertex> layer3 = ImmutableList.of(vx, vy);
    List<List<Vertex>> vertexListPerLayer = ImmutableList.of(layer1, layer2, layer3);

    List<Pair<Vertex, Vertex>> pairs = tgs.createPairs(vertexListPerLayer);
    Log.info("pairs={}", pairs);
    assertThat(pairs).containsExactly(//
        Pair.of(v1, va), //
        Pair.of(v1, vb), //
        Pair.of(v2, va), //
        Pair.of(v2, vb), //
        Pair.of(v1, vx), //
        Pair.of(v1, vy), //
        Pair.of(v2, vx), //
        Pair.of(v2, vy), //
        Pair.of(va, vx), //
        Pair.of(va, vy), //
        Pair.of(vb, vx), //
        Pair.of(vb, vy) //
    );
  }

  @Test
  public void testHasSameTextRangePasses() throws Exception {
    Vertex firstTextSegment = mock(Vertex.class);
    List<Vertex> firstTextSegmentList = ImmutableList.of(firstTextSegment);

    Vertex lastTextSegment = mock(Vertex.class);
    List<Vertex> lastTextSegmentList = ImmutableList.of(lastTextSegment);

    Vertex v1 = mockVertex("1");
    when(v1.vertices(Direction.OUT, EdgeLabels.FIRST_TEXT_SEGMENT)).thenReturn(firstTextSegmentList.iterator());
    when(v1.vertices(Direction.OUT, EdgeLabels.LAST_TEXT_SEGMENT)).thenReturn(lastTextSegmentList.iterator());

    Vertex va = mockVertex("a");
    when(va.vertices(Direction.OUT, EdgeLabels.FIRST_TEXT_SEGMENT)).thenReturn(firstTextSegmentList.iterator());
    when(va.vertices(Direction.OUT, EdgeLabels.LAST_TEXT_SEGMENT)).thenReturn(lastTextSegmentList.iterator());

    Pair<Vertex, Vertex> pair = Pair.of(v1, va);
    assertThat(tgs.hasSameTextRange(pair)).isTrue();
  }

  @Test
  public void testHasSameTextRangeFails() throws Exception {
    Vertex firstTextSegment = mock(Vertex.class);
    List<Vertex> firstTextSegmentList = ImmutableList.of(firstTextSegment);

    Vertex lastTextSegment = mock(Vertex.class);
    List<Vertex> lastTextSegmentList = ImmutableList.of(lastTextSegment);

    Vertex v1 = mockVertex("1");
    when(v1.vertices(Direction.OUT, EdgeLabels.FIRST_TEXT_SEGMENT)).thenReturn(firstTextSegmentList.iterator());
    when(v1.vertices(Direction.OUT, EdgeLabels.LAST_TEXT_SEGMENT)).thenReturn(firstTextSegmentList.iterator());

    Vertex va = mockVertex("a");
    when(va.vertices(Direction.OUT, EdgeLabels.FIRST_TEXT_SEGMENT)).thenReturn(firstTextSegmentList.iterator());
    when(va.vertices(Direction.OUT, EdgeLabels.LAST_TEXT_SEGMENT)).thenReturn(lastTextSegmentList.iterator());

    Pair<Vertex, Vertex> pair = Pair.of(v1, va);
    assertThat(tgs.hasSameTextRange(pair)).isFalse();
  }

  private void assertExpectation(String xml, String expected, UUID resourceUUID, TextRangeAnnotation textRangeAnnotation) {
    Log.info("xml={}", xml);
    setAbsolutePosition(textRangeAnnotation);
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
    setAbsolutePosition(textRangeAnnotation);
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

  private void setAbsolutePosition(TextRangeAnnotation textRangeAnnotation) {
    setAbsolutePosition(textRangeAnnotation, 1);
  }

  private void setAbsolutePosition(TextRangeAnnotation textRangeAnnotation, int length) {
    Position position = textRangeAnnotation.getPosition();
    AbsolutePosition absolutePosition = new AbsolutePosition()//
        .setXmlId(position.getXmlId().get())//
        .setOffset(position.getOffset().orElse(1))//
        .setLength(position.getLength().orElse(length))//
    ;
    textRangeAnnotation.setAbsolutePosition(absolutePosition);
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

  private Vertex mockVertex(String value) {
    Vertex v1 = mock(Vertex.class);
    when(v1.value("name")).thenReturn(value);
    return v1;
  }

}
