package nl.knaw.huygens.alexandria.textgraph;

/*
 * #%L
 * alexandria-main
 * =======
 * Copyright (C) 2015 - 2017 Huygens ING (KNAW)
 * =======
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import static org.assertj.core.api.Assertions.assertThat;

import java.io.StringWriter;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import nl.knaw.huygens.Log;
import nl.knaw.huygens.alexandria.api.model.text.view.ElementView;
import nl.knaw.huygens.alexandria.api.model.text.view.ElementView.AttributeFunction;
import nl.knaw.huygens.alexandria.api.model.text.view.ElementView.AttributeMode;
import nl.knaw.huygens.alexandria.api.model.text.view.ElementView.ElementMode;
import nl.knaw.huygens.alexandria.api.model.text.view.TextView;
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
  public void testStreamTextGraphSegmentWithDefaultView() {
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
  public void testStreamTextGraphSegmentWithHideNoteView() {
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
  public void testStreamTextGraphSegmentWithHideNoteTagView() {
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
  public void testStreamTextGraphSegmentWithOnFirstAttributeFunction1() {
    ImmutableList<String> respPriority = ImmutableList.of("#a", "#b");
    String expected = "<note xml:id=\"note-a\" resp=\"#a\">note text</note>";
    assertFirstOfWorksAsExpected(respPriority, expected);
  }

  private void assertFirstOfWorksAsExpected(ImmutableList<String> respPriority, String expected) {
    TextView textView = new TextView();
    ElementView elementView = new ElementView()//
        .setElementMode(ElementMode.show)//
        .setPreCondition("resp", AttributeFunction.firstOf, respPriority)//
        .setAttributeMode(AttributeMode.showAll);
    textView.putElementView("note", elementView);
    TextGraphSegment segment = new TextGraphSegment();
    TextAnnotation noteA = new TextAnnotation("note", ImmutableMap.of("xml:id", "note-a", "resp", "#a"), 0);
    TextAnnotation noteB = new TextAnnotation("note", ImmutableMap.of("xml:id", "note-b", "resp", "#b"), 0);
    TextAnnotation noteC = new TextAnnotation("note", ImmutableMap.of("xml:id", "note-c", "resp", "#c"), 0);
    segment.setAnnotationsToOpen(ImmutableList.of(noteA, noteB, noteC));
    segment.setTextSegment("note text");
    segment.setAnnotationsToClose(ImmutableList.of(noteC, noteB, noteA));
    assertSegmentViewAsExpected(segment, textView, expected);
  }

  @Test
  public void testStreamTextGraphSegmentWithOnFirstAttributeFunction2() {
    ImmutableList<String> respPriority = ImmutableList.of("#b", "#a");
    String expected = "<note xml:id=\"note-b\" resp=\"#b\">note text</note>";
    assertFirstOfWorksAsExpected(respPriority, expected);
  }

  @Test
  public void testStreamTextGraphSegmentWithShowOnlyAttributeMode() {
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
