package nl.knaw.huygens.alexandria.textgraph;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.StringWriter;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import nl.knaw.huygens.Log;
import nl.knaw.huygens.alexandria.api.model.ElementView;
import nl.knaw.huygens.alexandria.api.model.ElementView.AttributeMode;
import nl.knaw.huygens.alexandria.api.model.ElementView.ElementMode;
import nl.knaw.huygens.alexandria.api.model.TextView;
import nl.knaw.huygens.alexandria.test.AlexandriaTest;
import nl.knaw.huygens.alexandria.textgraph.TextGraphUtil.TextViewContext;

public class TextGraphUtilTest extends AlexandriaTest {
  @Test
  public void testParse() {
    // given
    String xml = singleQuotesToDouble("<text>"//
        + "<div xml:id='div-1' lang='nl'>"//
        + "<p xml:id='p1' rend='red'>paragraph with <b><i rend='yes'>text</i></b></p>"//
        + "<p>two</p>"//
        + "</div><lb/>"//
        + "<div xml:id='div-2' lang='nl'>"//
        + "<p>three</p>"//
        + "</div>"//
        + "</text>");
    String expectedBaseLayer = singleQuotesToDouble("<text>"//
        + "<div xml:id='div-1' lang='nl'>"//
        + "<p xml:id='p1' rend='red'>paragraph with text</p>"//
        + "<p>two</p>"//
        + "</div><lb/>"//
        + "<div xml:id='div-2' lang='nl'>"//
        + "<p>three</p>"//
        + "</div>"//
        + "</text>");

    // when
    ParseResult result = TextGraphUtil.parse(xml);

    // then
    List<String> textSegments = result.getTextSegments();
    softly.assertThat(textSegments).containsExactly("paragraph with ", "text", "two", "", "three");

    Set<XmlAnnotation> xmlAnnotations = result.getXmlAnnotations();
    softly.assertThat(xmlAnnotations).hasSize(9);
    Log.info("annotations = \n\t{}", Joiner.on("\n\t").join(xmlAnnotations));

    // DeprecatedTextView baselayerDefinition = new DeprecatedTextView("baselayer").setIncludedElementDefinitions(//
    // Lists.newArrayList(//
    // ElementDefinition.withName("text"), //
    // ElementDefinition.withName("div"), //
    // ElementDefinition.withName("lb"), //
    // ElementDefinition.withName("p")//
    // ));
    // String baseLayer = TextGraphUtil.renderTextView(textSegments, xmlAnnotations, baselayerDefinition);
    // softly.assertThat(baseLayer).isEqualTo(expectedBaseLayer);
  }

  @Test
  public void teststreamTextGraphSegmentWithDefaultView() {
    TextView textView = new TextView();
    TextGraphSegment segment = new TextGraphSegment();
    TextAnnotation note = new TextAnnotation("note", ImmutableMap.of("xml:id", "note-1"), 0);
    segment.setAnnotationsToOpen(ImmutableList.of(note));
    segment.setTextSegment("note text");
    segment.setAnnotationsToClose(ImmutableList.of(note));
    String expected = "<note xml:id=\"note-1\">note text</note>";
    assertSegmentViewAsExpected(segment, textView, expected);
  }

  @Test
  public void teststreamTextGraphSegmentWithHideNoteView() {
    TextView textView = new TextView();
    textView.putElementView("note", new ElementView().setElementMode(ElementMode.hide));
    TextGraphSegment segment = new TextGraphSegment();
    TextAnnotation note = new TextAnnotation("note", ImmutableMap.of("xml:id", "note-1"), 0);
    segment.setAnnotationsToOpen(ImmutableList.of(note));
    segment.setTextSegment("note text");
    segment.setAnnotationsToClose(ImmutableList.of(note));
    String expected = "";
    assertSegmentViewAsExpected(segment, textView, expected);
  }

  @Test
  public void teststreamTextGraphSegmentWithHideNoteTagView() {
    TextView textView = new TextView();
    textView.putElementView("note", new ElementView().setElementMode(ElementMode.hideTag));
    TextGraphSegment segment = new TextGraphSegment();
    TextAnnotation note = new TextAnnotation("note", ImmutableMap.of("xml:id", "note-1"), 0);
    segment.setAnnotationsToOpen(ImmutableList.of(note));
    segment.setTextSegment("note text");
    segment.setAnnotationsToClose(ImmutableList.of(note));
    String expected = "note text";
    assertSegmentViewAsExpected(segment, textView, expected);
  }

  @Test
  public void teststreamTextGraphSegmentWithShowOnlyAttributeMode() {
    TextView textView = new TextView();
    ElementView elementView = new ElementView()//
        .setElementMode(ElementMode.show)//
        .setAttributeMode(AttributeMode.showOnly)//
        .setRelevantAttributes(ImmutableList.of("xml:id"));
    textView.putElementView("note", elementView);
    TextGraphSegment segment = new TextGraphSegment();
    TextAnnotation note = new TextAnnotation("note", ImmutableMap.of("xml:id", "note-1", "key", "value"), 0);
    segment.setAnnotationsToOpen(ImmutableList.of(note));
    segment.setTextSegment("note text");
    segment.setAnnotationsToClose(ImmutableList.of(note));
    String expected = "<note xml:id=\"note-1\">note text</note>";
    assertSegmentViewAsExpected(segment, textView, expected);
  }

  private void assertSegmentViewAsExpected(TextGraphSegment segment, TextView textView, String expected) {
    TextViewContext context = new TextViewContext(textView);
    StringWriter writer = new StringWriter();
    TextGraphUtil.streamTextGraphSegment(writer, segment, context);
    String result = writer.toString();
    assertThat(result).isEqualTo(expected);
  }
}
