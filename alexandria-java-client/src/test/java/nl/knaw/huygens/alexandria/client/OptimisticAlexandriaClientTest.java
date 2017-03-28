package nl.knaw.huygens.alexandria.client;

/*
 * #%L
 * alexandria-java-client
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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import nl.knaw.huygens.Log;
import nl.knaw.huygens.alexandria.api.model.AboutEntity;
import nl.knaw.huygens.alexandria.api.model.Annotator;
import nl.knaw.huygens.alexandria.api.model.text.*;
import nl.knaw.huygens.alexandria.api.model.text.TextRangeAnnotation.Position;
import nl.knaw.huygens.alexandria.api.model.text.view.ElementView;
import nl.knaw.huygens.alexandria.api.model.text.view.ElementView.ElementMode;
import nl.knaw.huygens.alexandria.api.model.text.view.ElementViewDefinition;
import nl.knaw.huygens.alexandria.api.model.text.view.TextViewDefinition;
import nl.knaw.huygens.alexandria.api.model.text.view.TextViewList;
import nl.knaw.huygens.alexandria.client.model.ResourcePrototype;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URI;
import java.text.MessageFormat;
import java.util.*;

import static java.util.stream.Collectors.joining;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

public class OptimisticAlexandriaClientTest extends AlexandriaTestWithTestServer {

  private static final String EVERYTHING_UPTO_AND_INCLUDING_THE_LAST_PERIOD_REGEX = ".*\\.";
  private static OptimisticAlexandriaClient client;

  @BeforeClass
  public static void startClient() {
    client = new OptimisticAlexandriaClient("http://localhost:2016/");
  }

  @AfterClass
  public static void stopClient() {
    client.close();
  }

  @Before
  public void before() {
    client.setAuthKey(AUTHKEY);
    client.setAutoConfirm(true);
  }

  @Test
  public void testAbout() {
    AboutEntity about = client.getAbout();
    assertThat(about.getVersion()).isNotEmpty();
  }

  @Test
  public void testOptimisticAlexandriaClientHasDelegatedUnwrappedMethodForEachRelevantMethodInAlexandriaClient() {
    Class<AlexandriaClient> a = AlexandriaClient.class;

    String stubs = Arrays.stream(a.getMethods())//
        .filter(this::returnsRestResult)//
        .filter(this::hasNoDelegatedMethodInOptimisticAlexandriaClient)//
        .map(this::toDelegatedMethodStub)//
        .collect(joining("\n"));
    Log.info("Methods to add to OptimisticAlexandriaClient:\n{}", stubs);
    assertThat(stubs).isEmpty();
  }

  @Test
  public void testChangingExistingAttributesOnTextRangeAnnotationIsAllowedWhenAnnotatorAndPositionIsTheSame() {
    String xml = singleQuotesToDouble("<text><p xml:id='p-1'>This is a simple paragraph.</p></text>");
    UUID resourceUUID = createResourceWithText(xml);
    client.setAnnotator(resourceUUID, "ed", new Annotator().setCode("ed").setDescription("Eddy Wally"));

    UUID annotationUUID = UUID.randomUUID();
    Map<String, String> attributes1 = new HashMap<>();
    attributes1.put("key1", "value1");
    attributes1.put("key2", "value2");
    Position position = new Position()//
        .setXmlId("p-1");
    TextRangeAnnotation textRangeAnnotation = new TextRangeAnnotation()//
        .setId(annotationUUID)//
        .setName("tag")//
        .setAnnotator("ed")//
        .setPosition(position)//
        .setAttributes(attributes1);
    TextRangeAnnotationInfo info = client.setResourceTextRangeAnnotation(resourceUUID, textRangeAnnotation);
    assertThat(info.getAnnotates()).isEqualTo("This is a simple paragraph.");

    String annotatedXML = client.getTextAsString(resourceUUID);
    String expectation2 = singleQuotesToDouble("<text><p xml:id='p-1'><tag key1='value1' key2='value2' resp='#ed'>This is a simple paragraph.</tag></p></text>");
    assertThat(annotatedXML).isEqualTo(expectation2);

    // now to change the attributes of this annotation
    Map<String, String> attributes2 = new HashMap<>();
    attributes2.put("key1", "something");
    attributes2.put("key2", "entirely");
    textRangeAnnotation.setAttributes(attributes2);
    TextRangeAnnotationInfo info2 = client.setResourceTextRangeAnnotation(resourceUUID, textRangeAnnotation);
    Log.info("{}", info2);

    annotatedXML = client.getTextAsString(resourceUUID);
    String expectation3 = singleQuotesToDouble("<text><p xml:id='p-1'><tag key1='something' key2='entirely' resp='#ed'>This is a simple paragraph.</tag></p></text>");
    assertThat(annotatedXML).isEqualTo(expectation3);
  }

  @Test
  public void testAddingAttributesOnTextRangeAnnotationIsNotAllowed() {
    String xml = singleQuotesToDouble("<text><p xml:id='p-1'>This is a simple paragraph.</p></text>");
    UUID resourceUUID = createResourceWithText(xml);
    client.setAnnotator(resourceUUID, "ed", new Annotator().setCode("ed").setDescription("Eddy Wally"));

    UUID annotationUUID = UUID.randomUUID();
    Map<String, String> attributes1 = new HashMap<>();
    attributes1.put("key1", "value1");
    attributes1.put("key2", "value2");
    Position position = new Position()//
        .setXmlId("p-1");
    TextRangeAnnotation textRangeAnnotation = new TextRangeAnnotation()//
        .setId(annotationUUID)//
        .setName("tag")//
        .setAnnotator("ed")//
        .setPosition(position)//
        .setAttributes(attributes1);
    TextRangeAnnotationInfo info = client.setResourceTextRangeAnnotation(resourceUUID, textRangeAnnotation);
    assertThat(info.getAnnotates()).isEqualTo("This is a simple paragraph.");

    String annotatedXML = client.getTextAsString(resourceUUID);
    String expectation2 = singleQuotesToDouble("<text><p xml:id='p-1'><tag key1='value1' key2='value2' resp='#ed'>This is a simple paragraph.</tag></p></text>");
    assertThat(annotatedXML).isEqualTo(expectation2);

    // now to change the attributes of this annotation
    Map<String, String> attributes2 = new HashMap<>();
    attributes2.put("key1", "something");
    attributes2.put("key3", "entirely");
    textRangeAnnotation.setAttributes(attributes2);
    try {
      TextRangeAnnotationInfo info2 = client.setResourceTextRangeAnnotation(resourceUUID, textRangeAnnotation);
      Log.info("{}", info2);
      fail();
    } catch (AlexandriaException e) {
      assertThat(e.getMessage()).isEqualTo("400: You're only allowed to change existing attributes [key1, key2]");
    }

    annotatedXML = client.getTextAsString(resourceUUID);
    assertThat(annotatedXML).isEqualTo(expectation2);
  }

  @Test
  public void testOverlappingAnnotationsAreNotAllowed() {
    String xml = singleQuotesToDouble("<text><p xml:id='p-1'>This is a simple paragraph.</p></text>");
    UUID resourceUUID = createResourceWithText(xml);
    client.setAnnotator(resourceUUID, "ed", new Annotator().setCode("ed").setDescription("Eddy Wally"));

    UUID annotationUUID = UUID.randomUUID();
    Map<String, String> attributes1 = new HashMap<>();
    attributes1.put("key1", "value1");
    attributes1.put("key2", "value2");
    Position position = new Position()//
        .setXmlId("p-1");
    TextRangeAnnotation textRangeAnnotation = new TextRangeAnnotation()//
        .setId(annotationUUID)//
        .setName("tag")//
        .setAnnotator("ed")//
        .setPosition(position)//
        .setAttributes(attributes1);
    TextRangeAnnotationInfo info = client.setResourceTextRangeAnnotation(resourceUUID, textRangeAnnotation);
    assertThat(info.getAnnotates()).isEqualTo("This is a simple paragraph.");

    String annotatedXML = client.getTextAsString(resourceUUID);
    String expectation2 = singleQuotesToDouble("<text><p xml:id='p-1'><tag key1='value1' key2='value2' resp='#ed'>This is a simple paragraph.</tag></p></text>");
    assertThat(annotatedXML).isEqualTo(expectation2);

    // now try another annotation with the same position, name and annotator
    Map<String, String> attributes2 = new HashMap<>();
    attributes2.put("key1", "something");
    attributes2.put("key2", "different");
    UUID annotationUUID2 = UUID.randomUUID();
    TextRangeAnnotation textRangeAnnotation2 = new TextRangeAnnotation()//
        .setId(annotationUUID2)//
        .setName("tag")//
        .setAnnotator("ed")//
        .setPosition(position)//
        .setAttributes(attributes2);
    try {
      TextRangeAnnotationInfo info2 = client.setResourceTextRangeAnnotation(resourceUUID, textRangeAnnotation2);
      Log.info("{}", info2);
      fail();
    } catch (AlexandriaException e) {
      assertThat(e.getMessage()).isEqualTo("409: Overlapping annotations with the same name and responsibility.");
    }

    annotatedXML = client.getTextAsString(resourceUUID);
    assertThat(annotatedXML).isEqualTo(expectation2);
  }

  @Test
  public void testBugNLA324() {
    String rootXml = singleQuotesToDouble("<text><p xml:id='p-1'>I AM ROOT</p><ignore>this</ignore></text>");
    UUID rootResourceUUID = createResourceWithText(rootXml);

    ElementViewDefinition evd = new ElementViewDefinition()//
        .setElementMode(ElementMode.hide);
    TextViewDefinition textView = new TextViewDefinition()//
        .setDescription("ignore")//
        .setElementViewDefinition("ignore", evd);
    client.setResourceTextView(rootResourceUUID, "view1", textView);
    TextViewDefinition resourceTextView = client.getResourceTextView(rootResourceUUID, "view1");
    TextViewList resourceTextViews = client.getResourceTextViews(rootResourceUUID);
    Log.info("resourceTextView={}", resourceTextView);
    assertThat(resourceTextViews.isEmpty()).isFalse();

    String rootView = client.getTextAsString(rootResourceUUID, "view1");
    Log.info("rootView = {}", rootView);

    UUID letterUUID = client.addSubResource(rootResourceUUID, "letter01");
    String letterXml = singleQuotesToDouble("<text><p xml:id='p-1'>show this.</p><ignore>ignore this</ignore></text>");
    client.setResourceTextSynchronously(letterUUID, letterXml);

    String letterView = client.getTextAsString(letterUUID, "view1");
    Log.info("letterView = {}", letterView);
    String expected = singleQuotesToDouble("<text><p xml:id='p-1'>show this.</p></text>");
    assertThat(letterView).isEqualTo(expected);

    // now, change the textview
    textView.setElementViewDefinition("p", evd); // hide <p>
    client.setResourceTextView(rootResourceUUID, "view1", textView);
    String letterView2 = client.getTextAsString(letterUUID, "view1");
    Log.info("letterView = {}", letterView2);
    String expected2 = singleQuotesToDouble("<text></text>");
    assertThat(letterView2).isEqualTo(expected2);
  }

  @Test
  public void testNLA330() {
    String rootXml = singleQuotesToDouble("<text>"//
        + "<p xml:id='p-1'>"//
        + "Show a, ignore b &amp; c: "//
        + "<persName xml:id='persName-a1' resp='#a'>"//
        + "<persName xml:id='persName-b1' resp='#b'>"//
        + "<persName xml:id='persName-c1' resp='#c'>Sinterklaas</persName></persName></persName>"//
        + "</p>"//

        + "<p xml:id='p-2'>"//
        + "Show b, ignore c: "//
        + "<persName xml:id='persName-b2' resp='#b'>"//
        + "<persName xml:id='persName-c2' resp='#c'>Pietje Puk</persName></persName>"//
        + "</p>"//

        + "<p xml:id='p-3'>"//
        + "Ignore c: "//
        + "<persName xml:id='persName-c3' resp='#c'>Amerigo</persName>"//
        + "</p>"//
        + "</text>");
    UUID rootResourceUUID = createResourceWithText(rootXml);

    ElementViewDefinition evd = new ElementViewDefinition()//
        .setElementMode(ElementMode.show)//
        .setWhen("attribute(resp).firstOf('#a','#b')");
    TextViewDefinition textView = new TextViewDefinition()//
        .setDescription("show-resp-a-or-b")//
        .setElementViewDefinition("persName", evd);
    client.setResourceTextView(rootResourceUUID, "view1", textView);

    String rootView = client.getTextAsString(rootResourceUUID, "view1");
    Log.info("rootView = {}", rootView);

    String expected = singleQuotesToDouble("<text>"//
        + "<p xml:id='p-1'>"//
        + "Show a, ignore b &amp; c: "//
        + "<persName xml:id='persName-a1' resp='#a'>Sinterklaas</persName>"//
        + "</p>"//

        + "<p xml:id='p-2'>"//
        + "Show b, ignore c: "//
        + "<persName xml:id='persName-b2' resp='#b'>Pietje Puk</persName>"//
        + "</p>"//

        + "<p xml:id='p-3'>"//
        + "Ignore c: "//
        + "Amerigo"//
        + "</p>"//
        + "</text>");
    assertThat(rootView).isEqualTo(expected);
  }

  @Test
  public void testParameterizedView() {
    String rootXml = singleQuotesToDouble("<text>"//
        + "<p xml:id='p-1'>"//
        + "Show a, ignore b &amp; c: "//
        + "<persName xml:id='persName-a1' resp='#a'>"//
        + "<persName xml:id='persName-b1' resp='#b'>"//
        + "<persName xml:id='persName-c1' resp='#c'>Sinterklaas</persName></persName></persName>"//
        + "</p>"//

        + "<p xml:id='p-2'>"//
        + "Show b, ignore c: "//
        + "<persName xml:id='persName-b2' resp='#b'>"//
        + "<persName xml:id='persName-c2' resp='#c'>Pietje Puk</persName></persName>"//
        + "</p>"//

        + "<p xml:id='p-3'>"//
        + "Ignore c: "//
        + "<persName xml:id='persName-c3' resp='#c'>Amerigo</persName>"//
        + "</p>"//
        + "</text>");
    UUID rootResourceUUID = createResourceWithText(rootXml);

    ElementViewDefinition evd = new ElementViewDefinition()//
        .setElementMode(ElementMode.show)//
        .setWhen("attribute({a}).firstOf({list})");
    TextViewDefinition textView = new TextViewDefinition()//
        .setDescription("show-resp-a-or-b")//
        .setElementViewDefinition("persName", evd);
    client.setResourceTextView(rootResourceUUID, "view1", textView);

    Map<String, String> viewParameters = ImmutableMap.of("list", "'#a','#b'", "a", "resp");
    TextViewDefinition resourceTextViewDefinition = client.getResourceTextView(rootResourceUUID, "view1", viewParameters);
    Optional<String>when = resourceTextViewDefinition.getElementViewDefinitions().get("persName").getWhen();
    assertThat(when.get()).isEqualTo("attribute(resp).firstOf('#a','#b')");

    String rootView = client.getTextAsString(rootResourceUUID, "view1", viewParameters);
    Log.info("rootView = {}", rootView);

    String expected = singleQuotesToDouble("<text>"//
        + "<p xml:id='p-1'>"//
        + "Show a, ignore b &amp; c: "//
        + "<persName xml:id='persName-a1' resp='#a'>Sinterklaas</persName>"//
        + "</p>"//

        + "<p xml:id='p-2'>"//
        + "Show b, ignore c: "//
        + "<persName xml:id='persName-b2' resp='#b'>Pietje Puk</persName>"//
        + "</p>"//

        + "<p xml:id='p-3'>"//
        + "Ignore c: "//
        + "Amerigo"//
        + "</p>"//
        + "</text>");
    assertThat(rootView).isEqualTo(expected);
  }

  @Test
  public void testTextRangeAnnotationBatch() {
    String xml = singleQuotesToDouble("<text><p xml:id='p-1'>This is another simple paragraph.</p><p xml:id='p-2'>And another one.</p></text>");
    UUID resourceUUID = createResourceWithText(xml);
    client.setAnnotator(resourceUUID, "ed", new Annotator().setCode("ed").setDescription("Eddy Wally"));
    client.setAnnotator(resourceUUID, "eddie", new Annotator().setCode("eddie").setDescription("Eddie Christiani"));

    Map<String, String> attributes1 = new HashMap<>();
    attributes1.put("key1", "value1");
    attributes1.put("key2", "value2");
    Position position = new Position().setXmlId("p-1");
    UUID annotationUUID1 = UUID.randomUUID();
    TextRangeAnnotation textRangeAnnotation1 = new TextRangeAnnotation()//
        .setId(annotationUUID1)//
        .setName("tag")//
        .setAnnotator("ed")//
        .setPosition(position)//
        .setAttributes(attributes1)//
    ;
    UUID annotationUUID2 = UUID.randomUUID();
    TextRangeAnnotation textRangeAnnotation2 = new TextRangeAnnotation()//
        .setId(annotationUUID2)//
        .setName("aid")//
        .setAnnotator("ed")//
        .setPosition(position)//
    ;
    Position position2 = new Position().setXmlId("p-2");
    UUID annotationUUID3 = UUID.randomUUID();
    TextRangeAnnotation textRangeAnnotation3 = new TextRangeAnnotation()//
        .setId(annotationUUID3)//
        .setName("something")//
        .setAnnotator("eddie")//
        .setPosition(position2)//
    ;

    TextRangeAnnotationList textAnnotations = new TextRangeAnnotationList();
    textAnnotations.add(textRangeAnnotation1);
    textAnnotations.add(textRangeAnnotation2);
    textAnnotations.add(textRangeAnnotation3);
    client.addResourceTextRangeAnnotationsSynchronously(resourceUUID, textAnnotations);

    String annotatedText = client.getTextAsString(resourceUUID);
    String expectedAnnotatedText = singleQuotesToDouble("<text>"//
        + "<p xml:id='p-1'><aid resp='#ed'><tag key1='value1' key2='value2' resp='#ed'>This is another simple paragraph.</tag></aid></p>"//
        + "<p xml:id='p-2'><something resp='#eddie'>And another one.</something></p>"//
        + "</text>");
    assertThat(annotatedText).isEqualTo(expectedAnnotatedText);

    TextAnnotationImportStatus status = client.getResourceTextRangeAnnotationBatchImportStatus(resourceUUID);
    assertThat(status.getErrors()).isEmpty();
    Map<UUID, TextRangeAnnotationInfo> textRangeAnnotationInfoMap = status.getTextRangeAnnotationInfoMap();
    assertThat(textRangeAnnotationInfoMap).hasSize(3);
    assertThat(textRangeAnnotationInfoMap.get(annotationUUID1).getAnnotates()).isEqualTo("This is another simple paragraph.");
    assertThat(textRangeAnnotationInfoMap.get(annotationUUID2).getAnnotates()).isEqualTo("This is another simple paragraph.");
    assertThat(textRangeAnnotationInfoMap.get(annotationUUID3).getAnnotates()).isEqualTo("And another one.");
  }

  @Test
  public void testTextRangeAnnotationBatchWithNonNestingOverlapFails() {
    String xml = singleQuotesToDouble("<text><p xml:id='p-1'>This is another simple paragraph.</p></text>");
    UUID resourceUUID = createResourceWithText(xml);
    client.setAnnotator(resourceUUID, "ed", new Annotator().setCode("ed").setDescription("Eddy Wally"));

    Position position1 = new Position().setXmlId("p-1").setOffset(1).setLength(7); // This is
    UUID annotationUUID1 = UUID.randomUUID();
    TextRangeAnnotation textRangeAnnotation1 = new TextRangeAnnotation()//
        .setId(annotationUUID1)//
        .setName("tag")//
        .setAnnotator("ed")//
        .setPosition(position1)//
    ;

    Position position2 = new Position().setXmlId("p-1").setOffset(6).setLength(10); // is another
    UUID annotationUUID2 = UUID.randomUUID();
    TextRangeAnnotation textRangeAnnotation2 = new TextRangeAnnotation()//
        .setId(annotationUUID2)//
        .setName("tag")//
        .setAnnotator("ed")//
        .setPosition(position2)//
    ;

    TextRangeAnnotationList textAnnotations = new TextRangeAnnotationList();
    textAnnotations.add(textRangeAnnotation1);
    textAnnotations.add(textRangeAnnotation2);
    try {
      client.addResourceTextRangeAnnotationsSynchronously(resourceUUID, textAnnotations);
      fail();
    } catch (AlexandriaException e) {
      assertThat(e.getMessage()).isEqualTo("409 Overlapping annotations with the same name and responsibility.");
    }
  }

  @Test
  public void testBugNLA332() {
    String xml = singleQuotesToDouble("<text><p xml:id='p-1'>This is a simple paragraph.</p></text>");
    UUID resourceUUID = createResourceWithText(xml);
    client.setAnnotator(resourceUUID, "ed", new Annotator().setCode("ed").setDescription("Eddy Wally"));
    client.getTextAsDot(resourceUUID);

    UUID annotationUUID1 = UUID.randomUUID();
    Position position1 = new Position()//
        .setXmlId("p-1")//
        .setOffset(1)//
        .setLength(0);
    TextRangeAnnotation startMilestone = new TextRangeAnnotation()//
        .setId(annotationUUID1)//
        .setName("milestone-start")//
        .setAnnotator("ed")//
        .setPosition(position1)//
    ;

    UUID annotationUUID2 = UUID.randomUUID();
    Position position2 = new Position()//
        .setXmlId("p-1")//
        .setOffset(5)//
        .setLength(0);
    TextRangeAnnotation middleMilestone = new TextRangeAnnotation()//
        .setId(annotationUUID2)//
        .setName("milestone-middle")//
        .setAnnotator("ed")//
        .setPosition(position2)//
    ;

    UUID annotationUUID3 = UUID.randomUUID();
    Position position3 = new Position()//
        .setXmlId("p-1")//
        .setOffset(28)//
        .setLength(0);
    TextRangeAnnotation endMilestone = new TextRangeAnnotation()//
        .setId(annotationUUID3)//
        .setName("milestone-end")//
        .setAnnotator("ed")//
        .setPosition(position3)//
    ;

    TextRangeAnnotationList annotations = new TextRangeAnnotationList();
    annotations.add(startMilestone);
    annotations.add(middleMilestone);
    annotations.add(endMilestone);
    TextAnnotationImportStatus status = client.addResourceTextRangeAnnotationsSynchronously(resourceUUID, annotations);
    assertThat(status.getErrors()).isEmpty();

    client.getTextAsDot(resourceUUID);
    String annotatedXML = client.getTextAsString(resourceUUID);
    String expectation2 = singleQuotesToDouble("<text><p xml:id='p-1'><milestone-start resp='#ed'/>This<milestone-middle resp='#ed'/> is a simple paragraph.<milestone-end resp='#ed'/></p></text>");
    assertThat(annotatedXML).isEqualTo(expectation2);
  }

  @Test
  public void testBugNLA332a() {
    String xml = singleQuotesToDouble("<TEI>\n"//
        + "<text lang='la'>\n"//
        + "<body>\n"//
        + "<div type='letter'>\n"//
        + "<p xml:id='p-1'>...  ... Salmurij ...</p>\n"//
        + "<p xml:id='p-2'><figure><graphic url='beec002jour04ill02.gif'/></figure></p>\n"//
        + "</div>\n"//
        + "</body>\n"//
        + "</text>\n"//
        + "</TEI>");
    UUID resourceUUID = createResourceWithText(xml);
    client.setAnnotator(resourceUUID, "ckcc", new Annotator().setCode("ckcc").setDescription("CKCC project team"));
    client.setAnnotator(resourceUUID, "nerf", new Annotator().setCode("nerf").setDescription("Something"));
    client.getTextAsDot(resourceUUID);

    UUID annotationUUID1 = UUID.randomUUID();
    Position position1 = new Position()//
        .setXmlId("p-2");
    Map<String, String> attributes1 = ImmutableMap.of("value", "closer");
    TextRangeAnnotation closerAnnotation = new TextRangeAnnotation()//
        .setId(annotationUUID1)//
        .setName("p_type")//
        .setAnnotator("ckcc")//
        .setPosition(position1)//
        .setAttributes(attributes1);

    UUID annotationUUID2 = UUID.randomUUID();
    Position position2 = new Position()//
        .setXmlId("p-1")//
        .setOffset(5)//
        .setLength(0);
    TextRangeAnnotation placeNameAnnotation = new TextRangeAnnotation()//
        .setId(annotationUUID2)//
        .setName("placeName")//
        .setAnnotator("ckcc")//
        .setPosition(position2)//
    ;

    UUID annotationUUID3 = UUID.randomUUID();
    Position position3 = new Position()//
        .setXmlId("p-2");
    TextRangeAnnotation ptypeAnnotation2 = new TextRangeAnnotation()//
        .setId(annotationUUID3)//
        .setName("p_type")//
        .setAnnotator("nerf")//
        .setPosition(position3)//
    ;

    TextRangeAnnotationList annotations = new TextRangeAnnotationList();
    annotations.add(closerAnnotation);
    annotations.add(placeNameAnnotation);
    annotations.add(ptypeAnnotation2);
    TextAnnotationImportStatus status = client.addResourceTextRangeAnnotationsSynchronously(resourceUUID, annotations);
    assertThat(status.getErrors()).isEmpty();

    client.getTextAsDot(resourceUUID);
    String annotatedXML = client.getTextAsString(resourceUUID);
    String expectation2 = singleQuotesToDouble("<TEI>\n"//
        + "<text lang='la'>\n"//
        + "<body>\n"//
        + "<div type='letter'>\n"//
        + "<p xml:id='p-1'>... <placeName resp='#ckcc'/> ... Salmurij ...</p>\n"//
        + "<p xml:id='p-2'><p_type resp='#nerf'><p_type value='closer' resp='#ckcc'><figure><graphic url='beec002jour04ill02.gif'/></figure></p_type></p_type></p>\n"//
        + "</div>\n"//
        + "</body>\n"//
        + "</text>\n"//
        + "</TEI>");
    assertThat(annotatedXML).isEqualTo(expectation2);
  }

  @Test
  public void testBugNLA331() {
    String xml = singleQuotesToDouble("<text><p xml:id='p-1'>ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890</p><p xml:id='p-2'>And another one.</p></text>");
    UUID resourceUUID = createResourceWithText(xml);
    client.setAnnotator(resourceUUID, "ed", new Annotator().setCode("ed").setDescription("Eddy Wally"));

    int maxOffset = 60;
    int maxAnnotations = 1000;
    TextRangeAnnotationList annotations = new TextRangeAnnotationList();

    Random random2 = new Random();

    for (int i = 0; i < maxAnnotations; i++) {
      int randomOffset = random2.nextInt(maxOffset) + 1;
      UUID annotationUUID = UUID.randomUUID();
      Position position = new Position()//
          .setXmlId("p-1")//
          .setOffset(randomOffset)//
          .setLength(1); // so there's never an overlap
      TextRangeAnnotation randomAnnotation = new TextRangeAnnotation()//
          .setId(annotationUUID)//
          .setName("tag" + i)//
          .setAnnotator("ed")//
          .setPosition(position)//
      ;
      annotations.add(randomAnnotation);
    }

    TextAnnotationImportStatus status = client.addResourceTextRangeAnnotationsSynchronously(resourceUUID, annotations);
    assertThat(status.getErrors()).isEmpty();

    client.getTextAsDot(resourceUUID);
    String annotatedXML = client.getTextAsString(resourceUUID);
    assertThat(annotatedXML).isNotEmpty();
  }

  @Test
  public void testBugNLA332b() {
    String xml = singleQuotesToDouble("<TEI>\n"//
        + "<teiHeader>\n"//
        + "<meta type='uuid' value='cd89eced-78d9-4a6a-9fa1-3857011e8ede'/>\n"//
        + "<meta type='id' value='0001'/>\n"//
        + "<meta type='date' value='1612-07-18'/>\n"//
        + "<meta type='sender' value='beeckman.isaac.1588-1637'/>\n"//
        + "<meta type='senderloc' value='se:saumur.fra'/>\n"//
        + "<meta type='recipient' value='?'/>\n"//
        + "<meta type='recipientloc' value='?'/>\n"//
        + "<meta type='language' value='la'/>\n"//
        + "</teiHeader>\n"//

        + "<text xml:id='text-1' lang='la'>\n"//
        + "<body>\n"//
        + "<div xml:id='div-1' type='letter'>\n"//
        + "<p xml:id='p-1'>...  ... Salmurij ...</p>\n"//
        + "<p xml:id='p-2'><figure><graphic url='beec002jour04ill02.gif'/></figure></p>\n"//
        + "</div>\n"//
        + "</body>\n"//
        + "</text>\n"//
        + "</TEI>");
    UUID resourceUUID = createResourceWithText(xml);
    client.setAnnotator(resourceUUID, "ckcc", new Annotator().setCode("ckcc").setDescription("CKCC project team"));
    client.setAnnotator(resourceUUID, "nerf", new Annotator().setCode("nerf").setDescription("Something"));
    client.getTextAsDot(resourceUUID);

    UUID annotationUUID1 = UUID.randomUUID();
    Position position1 = new Position()//
        .setXmlId("p-2");
    Map<String, String> attributes1 = ImmutableMap.of("value", "closer");
    TextRangeAnnotation closerAnnotation = new TextRangeAnnotation()//
        .setId(annotationUUID1)//
        .setName("p_type")//
        .setAnnotator("ckcc")//
        .setPosition(position1)//
        .setAttributes(attributes1);
    TextRangeAnnotationInfo info1 = client.setResourceTextRangeAnnotation(resourceUUID, closerAnnotation);
    assertThat(info1.getAnnotates()).isEqualTo("");
    String textAfterFirstAnnotation = client.getTextAsString(resourceUUID);
    String expectation1 = singleQuotesToDouble("<TEI>\n"//
        + "<teiHeader>\n"//
        + "<meta type='uuid' value='cd89eced-78d9-4a6a-9fa1-3857011e8ede'/>\n"//
        + "<meta type='id' value='0001'/>\n"//
        + "<meta type='date' value='1612-07-18'/>\n"//
        + "<meta type='sender' value='beeckman.isaac.1588-1637'/>\n"//
        + "<meta type='senderloc' value='se:saumur.fra'/>\n"//
        + "<meta type='recipient' value='?'/>\n"//
        + "<meta type='recipientloc' value='?'/>\n"//
        + "<meta type='language' value='la'/>\n"//
        + "</teiHeader>\n"//

        + "<text xml:id='text-1' lang='la'>\n"//
        + "<body>\n"//
        + "<div xml:id='div-1' type='letter'>\n"//
        + "<p xml:id='p-1'>...  ... Salmurij ...</p>\n"//
        + "<p xml:id='p-2'><p_type value='closer' resp='#ckcc'><figure><graphic url='beec002jour04ill02.gif'/></figure></p_type></p>\n"//
        + "</div>\n"//
        + "</body>\n"//
        + "</text>\n"//
        + "</TEI>");
    assertThat(textAfterFirstAnnotation).isEqualTo(expectation1);

    UUID annotationUUID2 = UUID.randomUUID();
    Position position2 = new Position()//
        .setXmlId("p-1")//
        .setOffset(5)//
        .setLength(0);
    TextRangeAnnotation placeNameAnnotation = new TextRangeAnnotation()//
        .setId(annotationUUID2)//
        .setName("placeName")//
        .setAnnotator("ckcc")//
        .setPosition(position2)//
    ;
    TextRangeAnnotationInfo info2 = client.setResourceTextRangeAnnotation(resourceUUID, placeNameAnnotation);
    assertThat(info2.getAnnotates()).isEqualTo("");

    UUID annotationUUID3 = UUID.randomUUID();
    Position position3 = new Position()//
        .setXmlId("p-2");
    TextRangeAnnotation ptypeAnnotation2 = new TextRangeAnnotation()//
        .setId(annotationUUID3)//
        .setName("p_type")//
        .setAnnotator("nerf")//
        .setPosition(position3)//
    ;
    TextRangeAnnotationInfo info3 = client.setResourceTextRangeAnnotation(resourceUUID, ptypeAnnotation2);
    assertThat(info3.getAnnotates()).isEqualTo("");

    client.getTextAsDot(resourceUUID);
    String annotatedXML = client.getTextAsString(resourceUUID);
    String expectation2 = singleQuotesToDouble("<TEI>\n"//
        + "<teiHeader>\n"//
        + "<meta type='uuid' value='cd89eced-78d9-4a6a-9fa1-3857011e8ede'/>\n"//
        + "<meta type='id' value='0001'/>\n"//
        + "<meta type='date' value='1612-07-18'/>\n"//
        + "<meta type='sender' value='beeckman.isaac.1588-1637'/>\n"//
        + "<meta type='senderloc' value='se:saumur.fra'/>\n"//
        + "<meta type='recipient' value='?'/>\n"//
        + "<meta type='recipientloc' value='?'/>\n"//
        + "<meta type='language' value='la'/>\n"//
        + "</teiHeader>\n"//

        + "<text xml:id='text-1' lang='la'>\n"//
        + "<body>\n"//
        + "<div xml:id='div-1' type='letter'>\n"//
        + "<p xml:id='p-1'>... <placeName resp='#ckcc'/> ... Salmurij ...</p>\n"//
        + "<p xml:id='p-2'><p_type resp='#nerf'><p_type value='closer' resp='#ckcc'><figure><graphic url='beec002jour04ill02.gif'/></figure></p_type></p_type></p>\n"//
        + "</div>\n"//
        + "</body>\n"//
        + "</text>\n"//
        + "</TEI>");
    assertThat(annotatedXML).isEqualTo(expectation2);
  }

  @Test
  public void testBugNLA332bw() {
    String xml = singleQuotesToDouble("<TEI>\n"//
        + "<teiHeader>\n"//
        + "<meta type='uuid' value='cd89eced-78d9-4a6a-9fa1-3857011e8ede'/>\n"//
        + "<meta type='id' value='0001'/>\n"//
        + "</teiHeader>\n"//
        + "<text xml:id='text-1' lang='la'>\n"//
        + "<body>\n"//
        + "<div xml:id='div-1' type='letter'>\n"//
        + "<p xml:id='p-1'>...  ... Salmurij ...</p>\n"//
        + "<p xml:id='p-2'><figure><graphic url='beec002jour04ill02.gif'/></figure></p>\n"//
        + "</div>\n"//
        + "</body>\n"//
        + "</text>\n"//
        + "</TEI>");
    // ----
    // UUID resourceUUID = createResourceWithText(xml);
    // ----
    UUID resourceUUID = UUID.randomUUID();
    client.setResource(resourceUUID, resourceUUID.toString());
    setResourceText(resourceUUID, xml);
    // ----
    client.setAnnotator(resourceUUID, "ckcc", new Annotator().setCode("ckcc").setDescription("CKCC project team"));
    client.setAnnotator(resourceUUID, "nerf", new Annotator().setCode("nerf").setDescription("Something"));
    // ----
    client.getTextAsDot(resourceUUID);
    // ----
    System.out.println(client.getTextAsString(resourceUUID));
    // ----

    UUID annotationUUID1 = UUID.randomUUID();
    Position position1 = new Position()//
        .setXmlId("p-2");
    Map<String, String> attributes1 = ImmutableMap.of("value", "closer");
    TextRangeAnnotation closerAnnotation = new TextRangeAnnotation()//
        .setId(annotationUUID1)//
        .setName("p_type")//
        .setAnnotator("ckcc")//
        .setPosition(position1)//
        .setAttributes(attributes1);
    TextRangeAnnotationInfo info1 = client.setResourceTextRangeAnnotation(resourceUUID, closerAnnotation);
    // ----
    // assertThat(info1.getAnnotates()).isEqualTo("");
    // ---
    System.out.printf("annotated: [%s]%n", info1.getAnnotates());
    if (!info1.getAnnotates().equals("")) {
      System.out.println("ERROR");
    }
    // ----

    String textAfterFirstAnnotation = client.getTextAsString(resourceUUID);
    String expectation1 = singleQuotesToDouble("<TEI>\n"//
        + "<teiHeader>\n"//
        + "<meta type='uuid' value='cd89eced-78d9-4a6a-9fa1-3857011e8ede'/>\n"//
        + "<meta type='id' value='0001'/>\n"//
        + "</teiHeader>\n"//
        + "<text xml:id='text-1' lang='la'>\n"//
        + "<body>\n"//
        + "<div xml:id='div-1' type='letter'>\n"//
        + "<p xml:id='p-1'>...  ... Salmurij ...</p>\n"//
        + "<p xml:id='p-2'><p_type value='closer' resp='#ckcc'><figure><graphic url='beec002jour04ill02.gif'/></figure></p_type></p>\n"//
        + "</div>\n"//
        + "</body>\n"//
        + "</text>\n"//
        + "</TEI>");
    // ----
    assertThat(textAfterFirstAnnotation).isEqualTo(expectation1);
    // ----
    System.out.println(textAfterFirstAnnotation);
    if (!textAfterFirstAnnotation.equals(expectation1)) {
      System.out.println("ERROR");
    }
  }

  @Test
  public void testBugNLA340a() {
    String xml = singleQuotesToDouble("<TEI>\n"//
        + "<teiHeader>\n"//
        + "<meta type='uuid' value='cd89eced-78d9-4a6a-9fa1-3857011e8ede'/>\n"//
        + "<meta type='id' value='0001'/>\n"//
        + "</teiHeader>\n"//
        + "<text xml:id='text-1' lang='la'>\n"//
        + "<body>\n"//
        + "<div xml:id='div-1' type='letter'>\n"//
        + "<p xml:id='p-1'>... A. McKenna, Sur L.Esprit de M. Arnaud de Pierre Jurieu, Chroniques de Port-Royal, 47 (1998), p.179-238. ...</p>\n"//
        + "</div>\n"//
        + "</body>\n"//
        + "</text>\n"//
        + "</TEI>");
    UUID resourceUUID = UUID.randomUUID();
    client.setResource(resourceUUID, resourceUUID.toString());
    setResourceText(resourceUUID, xml);
    client.setAnnotator(resourceUUID, "ckcc", new Annotator().setCode("ckcc").setDescription("CKCC project team"));
    // client.getTextAsDot(resourceUUID);

    UUID annotationUUID1 = UUID.randomUUID();
    Position position1 = new Position()//
        .setXmlId("p-1")//
        .setOffset(5)//
        .setLength(54);
    TextRangeAnnotation titleAnnotation1 = new TextRangeAnnotation()//
        .setId(annotationUUID1)//
        .setName("title")//
        .setAnnotator("ckcc")//
        .setPosition(position1);
    TextRangeAnnotationInfo info1 = client.setResourceTextRangeAnnotation(resourceUUID, titleAnnotation1);
    assertThat(info1.getAnnotates()).isEqualTo("A. McKenna, Sur L.Esprit de M. Arnaud de Pierre Jurieu");
    System.out.printf("annotated: [%s]%n", info1.getAnnotates());

    String textAfterFirstAnnotation = client.getTextAsString(resourceUUID);
    String expectation1 = singleQuotesToDouble("<TEI>\n"//
        + "<teiHeader>\n"//
        + "<meta type='uuid' value='cd89eced-78d9-4a6a-9fa1-3857011e8ede'/>\n"//
        + "<meta type='id' value='0001'/>\n"//
        + "</teiHeader>\n"//
        + "<text xml:id='text-1' lang='la'>\n"//
        + "<body>\n"//
        + "<div xml:id='div-1' type='letter'>\n"//
        + "<p xml:id='p-1'>... <title resp='#ckcc'>A. McKenna, Sur L.Esprit de M. Arnaud de Pierre Jurieu</title>, Chroniques de Port-Royal, 47 (1998), p.179-238. ...</p>\n"//
        + "</div>\n"//
        + "</body>\n"//
        + "</text>\n"//
        + "</TEI>");
    assertThat(textAfterFirstAnnotation).isEqualTo(expectation1);

    UUID annotationUUID2 = UUID.randomUUID();
    Position position2 = new Position()//
        .setXmlId("p-1")//
        .setOffset(21)//
        .setLength(8);
    TextRangeAnnotation titleAnnotation2 = new TextRangeAnnotation()//
        .setId(annotationUUID2)//
        .setName("title")//
        .setAnnotator("ckcc")//
        .setPosition(position2);
    TextRangeAnnotationInfo info2 = client.setResourceTextRangeAnnotation(resourceUUID, titleAnnotation2);
    assertThat(info2.getAnnotates()).isEqualTo("L.Esprit");
    System.out.printf("annotated: [%s]%n", info2.getAnnotates());

    String textAfterSecondAnnotation = client.getTextAsString(resourceUUID);
    String expectation2 = singleQuotesToDouble("<TEI>\n"//
        + "<teiHeader>\n"//
        + "<meta type='uuid' value='cd89eced-78d9-4a6a-9fa1-3857011e8ede'/>\n"//
        + "<meta type='id' value='0001'/>\n"//
        + "</teiHeader>\n"//
        + "<text xml:id='text-1' lang='la'>\n"//
        + "<body>\n"//
        + "<div xml:id='div-1' type='letter'>\n"//
        + "<p xml:id='p-1'>... <title resp='#ckcc'>A. McKenna, Sur <title resp='#ckcc'>L.Esprit</title> de M. Arnaud de Pierre Jurieu</title>, Chroniques de Port-Royal, 47 (1998), p.179-238. ...</p>\n"//
        + "</div>\n"//
        + "</body>\n"//
        + "</text>\n"//
        + "</TEI>");
    assertThat(textAfterSecondAnnotation).isEqualTo(expectation2);
  }

  @Test
  public void testBugNLA340b() {
    String xml = singleQuotesToDouble("<TEI>\n"//
        + "<teiHeader>\n"//
        + "<meta type='uuid' value='cd89eced-78d9-4a6a-9fa1-3857011e8ede'/>\n"//
        + "<meta type='id' value='0001'/>\n"//
        + "</teiHeader>\n"//
        + "<text xml:id='text-1' lang='la'>\n"//
        + "<body>\n"//
        + "<div xml:id='div-1' type='letter'>\n"//
        + "<p xml:id='p-1'>Mijn spreekbeurt over De Avonden</p>\n"//
        + "</div>\n"//
        + "</body>\n"//
        + "</text>\n"//
        + "</TEI>");
    UUID resourceUUID = UUID.randomUUID();
    client.setResource(resourceUUID, resourceUUID.toString());
    setResourceText(resourceUUID, xml);
    client.setAnnotator(resourceUUID, "ckcc", new Annotator().setCode("ckcc").setDescription("CKCC project team"));
    // client.getTextAsDot(resourceUUID);

    UUID annotationUUID1 = UUID.randomUUID();
    Position position1 = new Position()//
        .setXmlId("p-1")//
        .setOffset(23)//
        .setLength(10);
    TextRangeAnnotation titleAnnotation1 = new TextRangeAnnotation()//
        .setId(annotationUUID1)//
        .setName("title")//
        .setAnnotator("ckcc")//
        .setPosition(position1);
    TextRangeAnnotationInfo info1 = client.setResourceTextRangeAnnotation(resourceUUID, titleAnnotation1);
    assertThat(info1.getAnnotates()).isEqualTo("De Avonden");
    System.out.printf("annotated: [%s]%n", info1.getAnnotates());

    String textAfterFirstAnnotation = client.getTextAsString(resourceUUID);
    String expectation1 = singleQuotesToDouble("<TEI>\n"//
        + "<teiHeader>\n"//
        + "<meta type='uuid' value='cd89eced-78d9-4a6a-9fa1-3857011e8ede'/>\n"//
        + "<meta type='id' value='0001'/>\n"//
        + "</teiHeader>\n"//
        + "<text xml:id='text-1' lang='la'>\n"//
        + "<body>\n"//
        + "<div xml:id='div-1' type='letter'>\n"//
        + "<p xml:id='p-1'>Mijn spreekbeurt over <title resp='#ckcc'>De Avonden</title></p>\n"//
        + "</div>\n"//
        + "</body>\n"//
        + "</text>\n"//
        + "</TEI>");
    assertThat(textAfterFirstAnnotation).isEqualTo(expectation1);

    UUID annotationUUID2 = UUID.randomUUID();
    Position position2 = new Position()//
        .setXmlId("p-1");
    TextRangeAnnotation titleAnnotation2 = new TextRangeAnnotation()//
        .setId(annotationUUID2)//
        .setName("title")//
        .setAnnotator("ckcc")//
        .setPosition(position2);
    TextRangeAnnotationInfo info2 = client.setResourceTextRangeAnnotation(resourceUUID, titleAnnotation2);
    assertThat(info2.getAnnotates()).isEqualTo("Mijn spreekbeurt over De Avonden");
    System.out.printf("annotated: [%s]%n", info2.getAnnotates());

    String textAfterSecondAnnotation = client.getTextAsString(resourceUUID);
    String expectation2 = singleQuotesToDouble("<TEI>\n"//
        + "<teiHeader>\n"//
        + "<meta type='uuid' value='cd89eced-78d9-4a6a-9fa1-3857011e8ede'/>\n"//
        + "<meta type='id' value='0001'/>\n"//
        + "</teiHeader>\n"//
        + "<text xml:id='text-1' lang='la'>\n"//
        + "<body>\n"//
        + "<div xml:id='div-1' type='letter'>\n"//
        + "<p xml:id='p-1'><title resp='#ckcc'>Mijn spreekbeurt over <title resp='#ckcc'>De Avonden</title></title></p>\n"//
        + "</div>\n"//
        + "</body>\n"//
        + "</text>\n"//
        + "</TEI>");
    assertThat(textAfterSecondAnnotation).isEqualTo(expectation2);
  }

  @Test
  public void testBugNLA340c() {
    // <title resp="#ed"><title resp="#ed">x</title></title> should not be accepted
    String xml = singleQuotesToDouble("<TEI>\n"//
        + "<text xml:id='text-1' lang='la'>\n"//
        + "<body>\n"//
        + "<div xml:id='div-1' type='letter'>\n"//
        + "<p xml:id='p-1'>... x ...</p>\n"//
        + "</div>\n"//
        + "</body>\n"//
        + "</text>\n"//
        + "</TEI>");
    UUID resourceUUID = UUID.randomUUID();
    client.setResource(resourceUUID, resourceUUID.toString());
    setResourceText(resourceUUID, xml);
    client.setAnnotator(resourceUUID, "ckcc", new Annotator().setCode("ckcc").setDescription("CKCC project team"));

    UUID annotationUUID1 = UUID.randomUUID();
    Position position1 = new Position()//
        .setXmlId("p-1")//
        .setOffset(5)//
        .setLength(1);
    TextRangeAnnotation titleAnnotation1 = new TextRangeAnnotation()//
        .setId(annotationUUID1)//
        .setName("title")//
        .setAnnotator("ckcc")//
        .setPosition(position1);
    TextRangeAnnotationInfo info1 = client.setResourceTextRangeAnnotation(resourceUUID, titleAnnotation1);
    assertThat(info1.getAnnotates()).isEqualTo("x");
    System.out.printf("annotated: [%s]%n", info1.getAnnotates());

    String textAfterFirstAnnotation = client.getTextAsString(resourceUUID);
    String expectation1 = singleQuotesToDouble("<TEI>\n"//
        + "<text xml:id='text-1' lang='la'>\n"//
        + "<body>\n"//
        + "<div xml:id='div-1' type='letter'>\n"//
        + "<p xml:id='p-1'>... <title resp='#ckcc'>x</title> ...</p>\n"//
        + "</div>\n"//
        + "</body>\n"//
        + "</text>\n"//
        + "</TEI>");
    assertThat(textAfterFirstAnnotation).isEqualTo(expectation1);

    UUID annotationUUID2 = UUID.randomUUID();
    TextRangeAnnotation titleAnnotation2 = new TextRangeAnnotation()//
        .setId(annotationUUID2)//
        .setName("title")//
        .setAnnotator("ckcc")//
        .setPosition(position1);
    try {
      TextRangeAnnotationInfo info2 = client.setResourceTextRangeAnnotation(resourceUUID, titleAnnotation2);
      fail();
    } catch (AlexandriaException e) {
      assertThat(e.getMessage()).isEqualTo("409: Overlapping annotations with the same name and responsibility.");
    }

    String textAfterSecondAnnotation = client.getTextAsString(resourceUUID);
    assertThat(textAfterSecondAnnotation).isEqualTo(expectation1); // text hasn't changed
  }

  @Test
  public void testNLA343() {
    String xml = singleQuotesToDouble("<TEI>\n"//
        + "<text xml:id='text-1' lang='la'>\n"//
        + "<body>\n"//
        + "<div xml:id='div-1' type='letter'>\n"//
        + "<p xml:id='p-1'>This is a naame with an error.</p>\n"//
        + "</div>\n"//
        + "</body>\n"//
        + "</text>\n"//
        + "</TEI>");
    UUID resourceUUID = UUID.randomUUID();
    client.setResource(resourceUUID, resourceUUID.toString());
    setResourceText(resourceUUID, xml);
    client.setAnnotator(resourceUUID, "ckcc", new Annotator().setCode("ckcc").setDescription("CKCC project team"));

    UUID annotationUUID1 = UUID.randomUUID();
    Position position = new Position()//
        .setXmlId("p-1")//
        .setOffset(11)//
        .setLength(5);
    TextRangeAnnotation sicAnnotation = new TextRangeAnnotation()//
        .setId(annotationUUID1)//
        .setName("sic")//
        .setAnnotator("ckcc")//
        .setPosition(position);
    TextRangeAnnotationInfo sicInfo = client.setResourceTextRangeAnnotation(resourceUUID, sicAnnotation);
    assertThat(sicInfo.getAnnotates()).isEqualTo("naame");
    System.out.printf("annotated: [%s]%n", sicInfo.getAnnotates());

    String textAfterFirstAnnotation = client.getTextAsString(resourceUUID);
    String expectation1 = singleQuotesToDouble("<TEI>\n"//
        + "<text xml:id='text-1' lang='la'>\n"//
        + "<body>\n"//
        + "<div xml:id='div-1' type='letter'>\n"//
        + "<p xml:id='p-1'>This is a <sic resp='#ckcc'>naame</sic> with an error.</p>\n"//
        + "</div>\n"//
        + "</body>\n"//
        + "</text>\n"//
        + "</TEI>");
    assertThat(textAfterFirstAnnotation).isEqualTo(expectation1);

    UUID annotationUUID2 = UUID.randomUUID();
    TextRangeAnnotation nameAnnotation = new TextRangeAnnotation()//
        .setId(annotationUUID2)//
        .setName("name")//
        .setAnnotator("ckcc")//
        .setPosition(position);
    TextRangeAnnotationInfo nameInfo = client.setResourceTextRangeAnnotation(resourceUUID, nameAnnotation);
    assertThat(nameInfo.getAnnotates()).isEqualTo("naame");
    System.out.printf("annotated: [%s]%n", sicInfo.getAnnotates());

    String textAfterSecondAnnotation = client.getTextAsString(resourceUUID);
    String expectation2 = singleQuotesToDouble("<TEI>\n"//
        + "<text xml:id='text-1' lang='la'>\n"//
        + "<body>\n"//
        + "<div xml:id='div-1' type='letter'>\n"//
        + "<p xml:id='p-1'>This is a <sic resp='#ckcc'><name resp='#ckcc'>naame</name></sic> with an error.</p>\n"//
        + "</div>\n"//
        + "</body>\n"//
        + "</text>\n"//
        + "</TEI>");
    assertThat(textAfterSecondAnnotation).isEqualTo(expectation2);

    Map<String, List<String>> annotationLayers = ImmutableMap.of(//
        "layer1", ImmutableList.of("sic"), //
        "layer2", ImmutableList.of("name") //
    );
    List<String> annotationLayerDepthOrder = ImmutableList.of("layer1", "layer2");
    TextViewDefinition sicInName = new TextViewDefinition()//
        .setAnnotationLayers(annotationLayers)//
        .setAnnotationLayerDepthOrder(annotationLayerDepthOrder);

    client.setResourceTextView(resourceUUID, "sicInName", sicInName);
    String viewXML = client.getTextAsString(resourceUUID, "sicInName");

    String expectation3 = singleQuotesToDouble("<TEI>\n"//
        + "<text xml:id='text-1' lang='la'>\n"//
        + "<body>\n"//
        + "<div xml:id='div-1' type='letter'>\n"//
        + "<p xml:id='p-1'>This is a <name resp='#ckcc'><sic resp='#ckcc'>naame</sic></name> with an error.</p>\n"//
        + "</div>\n"//
        + "</body>\n"//
        + "</text>\n"//
        + "</TEI>");
    assertThat(viewXML).isEqualTo(expectation3);
  }

  @Test
  public void testNLA343a() {
    String xml = singleQuotesToDouble("<TEI>\n"//
        + "<text xml:id='text-1' lang='la'>\n"//
        + "<body>\n"//
        + "<div xml:id=\"div-1\" type=\"letter\">\n"//
        + "<p xml:id=\"p-1\">...<hi rend=\"i\">BAYLE</hi>...</p>\n"//
        + "<p xml:id=\"p-2\">...BAYLE...</p>\n" //
        + "<p xml:id=\"p-3\">...<hi rend=\"i\">BAYLE</hi>...</p>\n"//
        + "<p xml:id=\"p-4\">...<hi rend=\"i\">BAYLE</hi>...</p>\n"//
        + "</div>\n"//
        + "</body>\n"//
        + "</text>\n"//
        + "</TEI>");
    UUID resourceUUID = UUID.randomUUID();
    client.setResource(resourceUUID, resourceUUID.toString());
    setResourceText(resourceUUID, xml);

    client.setAnnotator(resourceUUID, "ckcc", new Annotator().setCode("ckcc").setDescription("CKCC project team"));
    client.setResourceTextView(resourceUUID, "epistolarium", defaultDefinition());

    annotateBayle(resourceUUID, "p-4");
    annotateBayle(resourceUUID, "p-3");
    annotateBayle(resourceUUID, "p-2");
    // client.getTextAsDot(resourceUUID);

    // String original = client.getTextAsString(resourceUUID);
    // assertThat(original).contains("<hi rend=\"i\"><persName resp=\"#ckcc\"><persName_key value=\"bayle.jacob.1644-1685\" resp=\"#ckcc\">BAYLE</persName_key></persName></hi>");

    String view1 = client.getTextAsString(resourceUUID, "epistolarium", ImmutableMap.of("list", "'#ckcc','#abc'"));
    assertThat(view1).contains("<p xml:id=\"p-1\">...<hi rend=\"i\">BAYLE</hi>...</p>");
    assertThat(view1).contains("<p xml:id=\"p-2\">...<persName resp=\"#ckcc\"><persName_key value=\"bayle.jacob.1644-1685\" resp=\"#ckcc\">BAYLE</persName_key></persName>...</p>");
    assertThat(view1).contains("<p xml:id=\"p-3\">...<persName resp=\"#ckcc\"><persName_key value=\"bayle.jacob.1644-1685\" resp=\"#ckcc\"><hi rend=\"i\">BAYLE</hi></persName_key></persName>...</p>");
    assertThat(view1).contains("<p xml:id=\"p-4\">...<persName resp=\"#ckcc\"><persName_key value=\"bayle.jacob.1644-1685\" resp=\"#ckcc\"><hi rend=\"i\">BAYLE</hi></persName_key></persName>...</p>");
  }

  @Test
  public void testNLA367a(){
    String xml = singleQuotesToDouble("<title xml:id='jlc'>Joany loves Chachi</title>");
    UUID resourceUUID = UUID.randomUUID();
    client.setResource(resourceUUID, resourceUUID.toString());
    setResourceText(resourceUUID, xml);

    client.setAnnotator(resourceUUID, "ckcc", new Annotator().setCode("ckcc").setDescription("CKCC project team"));

    UUID annotationUUIDa = UUID.randomUUID();
    Position positionA = new Position()//
      .setXmlId("jlc")//
      .setOffset(1)//
      .setLength(11);
    TextRangeAnnotation aAnnotation = new TextRangeAnnotation()//
      .setId(annotationUUIDa)//
      .setName("a")//
      .setAnnotator("ckcc")//
      .setPosition(positionA);
    TextRangeAnnotationInfo aInfo = client.setResourceTextRangeAnnotation(resourceUUID, aAnnotation);

    UUID annotationUUIDb = UUID.randomUUID();
    Position positionB = new Position()//
      .setXmlId("jlc")//
      .setOffset(7)//
      .setLength(12);
    TextRangeAnnotation bAnnotation = new TextRangeAnnotation()//
      .setId(annotationUUIDb)//
      .setName("b")//
      .setAnnotator("ckcc")//
      .setPosition(positionB);
    TextRangeAnnotationInfo bInfo = client.setResourceTextRangeAnnotation(resourceUUID, bAnnotation);

    String xml2 = client.getTextAsString(resourceUUID);

    assertThat(xml2).isEqualTo("<title xml:id=\"jlc\"><a resp=\"#ckcc\">Joany <b resp=\"#ckcc\">loves</a> Chachi</b></title>");

  }

  private void annotateBayle(UUID resourceUUID, String xmlId) {
    UUID annotationUUID = UUID.randomUUID();
    Position position = new Position()//
        .setXmlId(xmlId)//
        .setOffset(4)//
        .setLength(5);
    TextRangeAnnotation persNameAnnotation = new TextRangeAnnotation()//
        .setId(annotationUUID)//
        .setName("persName")//
        .setAnnotator("ckcc")//
        .setPosition(position);
    TextRangeAnnotationInfo persNameInfo = client.setResourceTextRangeAnnotation(resourceUUID, persNameAnnotation);
    assertThat(persNameInfo.getAnnotates()).isEqualTo("BAYLE");

    UUID annotationUUIDkey = UUID.randomUUID();
    Position position2 = new Position()//
        .setTargetAnnotationId(annotationUUID);
    TextRangeAnnotation persNameKeyAnnotation = new TextRangeAnnotation()//
        .setId(annotationUUIDkey)//
        .setName("persName_key")//
        .setAnnotator("ckcc")//
        .setAttributes(ImmutableMap.of("value", "bayle.jacob.1644-1685"))//
        .setPosition(position2);
    TextRangeAnnotationInfo persNameKeyInfo = client.setResourceTextRangeAnnotation(resourceUUID, persNameKeyAnnotation);
    assertThat(persNameKeyInfo.getAnnotates()).isEqualTo("");
  }

  private TextViewDefinition defaultDefinition() {
    Map<String, List<String>> annotationLayers = ImmutableMap.of(//
        "correctionLayer", ImmutableList.of("corr", "sic"), //
        "formattingLayer", ImmutableList.of("hi") //
    );
    List<String> annotationLayerDepthOrder = ImmutableList.of("correctionLayer", "formattingLayer");

    ElementViewDefinition respPriority = new ElementViewDefinition() //
        .setElementMode(ElementMode.show) //
        .setAttributeMode(ElementView.AttributeMode.showAll.name()) // needed?
        .setWhen("attribute(resp).firstOf({list})");

    return new TextViewDefinition() //
        .setDescription("View used in ePistolarium") //
        // language annotations
        .setElementViewDefinition("head_lang", respPriority) //
        .setElementViewDefinition("lg_lang", respPriority) //
        .setElementViewDefinition("p_lang", respPriority) //
        .setElementViewDefinition("p_type", respPriority) //
        // named entity annotations
        .setElementViewDefinition("geogame_key", respPriority) //
        .setElementViewDefinition("orgName_key", respPriority) //
        .setElementViewDefinition("persName_key", respPriority) //
        .setElementViewDefinition("placeName_key", respPriority) //
        .setElementViewDefinition("rs_key", respPriority) //
        .setAnnotationLayers(annotationLayers) //
        .setAnnotationLayerDepthOrder(annotationLayerDepthOrder);
  }

  /// end tests

  private UUID createResourceWithText(String xml) {
    String resourceRef = "test";
    UUID resourceUUID = createResource(resourceRef);
    TextImportStatus textGraphImportStatus = setResourceText(resourceUUID, xml);
    URI expectedURI = URI.create("http://localhost:2016/resources/" + resourceUUID + "/text/xml");
    assertThat(textGraphImportStatus.getTextURI()).isEqualTo(expectedURI);
    return resourceUUID;
  }

  protected UUID createResource(String resourceRef) {
    ResourcePrototype resource = new ResourcePrototype().setRef(resourceRef);
    UUID resourceUuid = UUID.randomUUID();
    client.setResource(resourceUuid, resource);
    return resourceUuid;
  }

  boolean returnsRestResult(Method method) {
    return method.getReturnType().equals(RestResult.class);
  }

  boolean hasNoDelegatedMethodInOptimisticAlexandriaClient(Method method) {
    Class<OptimisticAlexandriaClient> o = OptimisticAlexandriaClient.class;
    try {
      Method oMethod = o.getMethod(method.getName(), method.getParameterTypes());
      Type type = actualReturnType(method);
      boolean equals = type.equals(Void.class)//
          ? oMethod.getReturnType().equals(Void.TYPE)//
          : oMethod.getReturnType().equals(type);
      return !equals;
    } catch (Exception e) {
      return true;
    }
  }

  private Type actualReturnType(Method method) {
    Type genericReturnType = method.getGenericReturnType();
    return ((ParameterizedType) genericReturnType).getActualTypeArguments()[0];
  }

  String toDelegatedMethodStub(Method method) {
    String returnType = actualReturnType(method).getTypeName().replaceFirst(EVERYTHING_UPTO_AND_INCLUDING_THE_LAST_PERIOD_REGEX, "").replace("Void", "void");
    String methodName = method.getName();
    String qualifiedParameters = Arrays.stream(method.getParameters())//
        .map(this::toQualifiedParameter)//
        .collect(joining(", "));
    String returnStatement = "void".equals(returnType) ? "" : "return ";
    String parameters = Arrays.stream(method.getParameters())//
        .map(this::parameterName)//
        .collect(joining(", "));

    return MessageFormat.format(//
        "public {0} {1}({2}) '{' {3}unwrap(delegate.{4}({5}));'}'", //
        returnType, //
        methodName, //
        qualifiedParameters, //
        returnStatement, //
        methodName, //
        parameters//
    );
  }

  String toQualifiedParameter(Parameter parameter) {
    return typeString(parameter) + " " + parameterName(parameter);
  }

  private String typeString(Parameter parameter) {
    return parameter.getType().getName().replaceFirst(EVERYTHING_UPTO_AND_INCLUDING_THE_LAST_PERIOD_REGEX, "");
  }

  String parameterName(Parameter parameter) {
    return typeString(parameter).toLowerCase();
  }

  protected TextImportStatus setResourceText(UUID resourceUuid, String xml) {
    return client.setResourceTextSynchronously(resourceUuid, xml);
  }

}
