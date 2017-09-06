package nl.knaw.huygens.antioch.client;

/*
 * #%L
 * antioch-java-client
 * =======
 * Copyright (C) 2015 - 2017 Huygens ING (KNAW)
 * =======
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

public class TextRangeAnnotationTest extends AntiochClientTest {
  // TODO move to markup module
  // @Before
  // public void before() {
  // client.setAuthKey(AUTHKEY);
  // }
  //
  // @Test
  // public void testSetTextRangeAnnotation() {
  // String xml = singleQuotesToDouble("<text><p xml:id='p-1'>This is a simple paragraph.</p></text>");
  // UUID resourceUUID = createResourceWithText(xml);
  // RestResult<Void> result = client.setAnnotator(resourceUUID, "ed", new Annotator().setCode("ed").setDescription("Eddy Wally"));
  // assertRequestSucceeded(result);
  //
  // UUID annotationUUID = UUID.randomUUID();
  // Position position = new Position()//
  // .setXmlId("p-1")//
  // .setOffset(6)//
  // .setLength(2);
  // AbsolutePosition absolutePosition = new AbsolutePosition()//
  // .setXmlId(position.getXmlId().get())//
  // .setOffset(position.getOffset().get())//
  // .setLength(position.getLength().get());
  // TextRangeAnnotation textRangeAnnotation0 = new TextRangeAnnotation()//
  // .setId(annotationUUID)//
  // .setName("word")//
  // .setAnnotator("ed")//
  // .setPosition(position)//
  // .setAbsolutePosition(absolutePosition);
  // RestResult<TextRangeAnnotationInfo> putResult = client.setResourceTextRangeAnnotation(resourceUUID, textRangeAnnotation0);
  // assertRequestSucceeded(putResult);
  // TextRangeAnnotationInfo info = putResult.get();
  // assertThat(info.getAnnotates()).isEqualTo("is");
  // dumpDb();
  //
  // RestResult<TextRangeAnnotation> getResult = client.getResourceTextRangeAnnotation(resourceUUID, annotationUUID);
  // assertRequestSucceeded(getResult);
  // TextRangeAnnotation textRangeAnnotation1 = getResult.get();
  // textRangeAnnotation0.setAbsolutePosition(null);
  // assertThat(textRangeAnnotation1).isEqualToComparingFieldByFieldRecursively(textRangeAnnotation0);
  // }
  //
  // @Test
  // public void testSetTextRangeAnnotationWithInvalidXmlId() {
  // String xml = singleQuotesToDouble("<text><p xml:id='p-1'>This is a simple &amp; short paragraph.</p></text>");
  // UUID resourceUUID = createResourceWithText(xml);
  // RestResult<Void> result = client.setAnnotator(resourceUUID, "ed", new Annotator().setCode("ed").setDescription("Eddy Wally"));
  // assertRequestSucceeded(result);
  //
  // UUID uuid = UUID.randomUUID();
  // Position position = new Position()//
  // .setXmlId("undefined")//
  // .setOffset(6)//
  // .setLength(2);
  // TextRangeAnnotation textAnnotation = new TextRangeAnnotation()//
  // .setId(uuid)//
  // .setName("word")//
  // .setAnnotator("ed")//
  // .setPosition(position);
  // RestResult<TextRangeAnnotationInfo> putResult = client.setResourceTextRangeAnnotation(resourceUUID, textAnnotation);
  // assertThat(putResult.hasFailed()).isTrue();
  // assertThat(putResult.getErrorMessage().get()).isEqualTo("The text does not contain an element with the specified xml:id.");
  // }
  //
  // @Test
  // public void testSetTextRangeAnnotationOnElementWithoutText() {
  // String xml = singleQuotesToDouble("<text><p xml:id='p-9'>Ex Musaeo, &amp; ...</p>\n"//
  // + "<p xml:id='p-10'>Tuus ...</p>\n"//
  // + "<p xml:id='p-11'>Prenez ...</p>\n"//
  // + "<p xml:id='p-12'>Je vous ....</p>\n"//
  // + "<p xml:id='p-13'>A <placeName key='se:saumur.fra'>Saumur</placeName>.</p>\n"//
  // + "<p xml:id='p-14'><figure><graphic url='beec002jour04ill02.gif'/></figure></p></text>");
  // UUID resourceUUID = createResourceWithText(xml);
  // RestResult<Void> result = client.setAnnotator(resourceUUID, "ed", new Annotator().setCode("ed").setDescription("Eddy Thor"));
  // assertRequestSucceeded(result);
  //
  // UUID uuid = UUID.randomUUID();
  // Position position = new Position()//
  // .setXmlId("p-14");
  // TextRangeAnnotation textRangeAnnotation = new TextRangeAnnotation()//
  // .setId(uuid)//
  // .setName("hi")//
  // .setAnnotator("ed")//
  // .setPosition(position);
  // RestResult<TextRangeAnnotationInfo> putResult = client.setResourceTextRangeAnnotation(resourceUUID, textRangeAnnotation);
  // putResult.getFailureCause().ifPresent(Log::info);
  // assertThat(putResult.hasFailed()).isFalse();
  // Log.info(putResult.get().toString());
  // }
  //
  // @Test
  // public void testSetNonOverlappingTextRangeAnnotations() {
  // String xml = singleQuotesToDouble("<text><p xml:id='p-9'>Ex Musaeo,...</p>\n"//
  // + "<p xml:id='p-10'>Tuus...</p>\n"//
  // + "<p xml:id='p-11'>Prenez ...</p>\n"//
  // + "<p xml:id='p-12'>Je vous ....</p>\n"//
  // + "<p xml:id='p-13'>A <placeName key='se:saumur.fra'>Saumur</placeName>.</p>\n"//
  // + "<p xml:id='p-14'><figure><graphic url='beec002jour04ill02.gif'/></figure></p></text>");
  // UUID resourceUUID = createResourceWithText(xml);
  // RestResult<Void> result = client.setAnnotator(resourceUUID, "ckcc", new Annotator().setCode("ckcc").setDescription("Cees Kacc"));
  // assertRequestSucceeded(result);
  //
  // RestResult<TextRangeAnnotationList> restResult = client.getResourceTextRangeAnnotations(resourceUUID);
  // assertThat(restResult.hasFailed()).isFalse();
  // TextRangeAnnotationList list = restResult.get();
  // assertThat(list).hasSize(0);
  //
  // UUID closer13 = UUID.randomUUID();
  // Position position = new Position()//
  // .setXmlId("p-13");
  // TextRangeAnnotation textRangeAnnotation = new TextRangeAnnotation()//
  // .setId(closer13)//
  // .setName("closer")//
  // .setAnnotator("ckcc")//
  // .setPosition(position);
  // RestResult<TextRangeAnnotationInfo> putResult = client.setResourceTextRangeAnnotation(resourceUUID, textRangeAnnotation);
  // putResult.getFailureCause().ifPresent(Log::info);
  // assertThat(putResult.hasFailed()).isFalse();
  // Log.info(putResult.get().toString());
  //
  // UUID closer9 = UUID.randomUUID();
  // Position position9 = new Position()//
  // .setXmlId("p-9");
  // TextRangeAnnotation textRangeAnnotation9 = new TextRangeAnnotation()//
  // .setId(closer9)//
  // .setName("closer")//
  // .setAnnotator("ckcc")//
  // .setPosition(position9);
  // RestResult<TextRangeAnnotationInfo> putResult9 = client.setResourceTextRangeAnnotation(resourceUUID, textRangeAnnotation9);
  // putResult9.getFailureCause().ifPresent(Log::info);
  // assertThat(putResult9.hasFailed()).isFalse();
  // Log.info(putResult.get().toString());
  //
  // // now, receive all textrangeannotations for this text.
  // RestResult<TextRangeAnnotationList> restResult2 = client.getResourceTextRangeAnnotations(resourceUUID);
  // assertThat(restResult2.hasFailed()).isFalse();
  // TextRangeAnnotationList list2 = restResult.get();
  // assertThat(list2).hasSize(0);
  // }
  //
  // @Test
  // public void testSetNonOverlappingAdjacentTextRangeAnnotations() {
  // String xml = singleQuotesToDouble("<text><p xml:id='p-1'>... Patri M. MersennoR. Descartes S.D...</p></text>");
  // UUID resourceUUID = createResourceWithText(xml);
  // RestResult<Void> result = client.setAnnotator(resourceUUID, "ckcc", new Annotator().setCode("ckcc").setDescription("Co Koccu"));
  // assertRequestSucceeded(result);
  //
  // RestResult<TextRangeAnnotationList> restResult = client.getResourceTextRangeAnnotations(resourceUUID);
  // assertThat(restResult.hasFailed()).isFalse();
  // TextRangeAnnotationList list = restResult.get();
  // assertThat(list).hasSize(0);
  //
  // UUID persName1 = UUID.randomUUID();
  // Position position1 = new Position()//
  // .setXmlId("p-1")//
  // .setOffset(5)//
  // .setLength(17);
  // TextRangeAnnotation textRangeAnnotation = new TextRangeAnnotation()//
  // .setId(persName1)//
  // .setName("persName")//
  // .setAnnotator("ckcc")//
  // .setPosition(position1);
  // RestResult<TextRangeAnnotationInfo> putResult1 = client.setResourceTextRangeAnnotation(resourceUUID, textRangeAnnotation);
  // putResult1.getFailureCause().ifPresent(Log::info);
  // assertThat(putResult1.hasFailed()).isFalse();
  // assertThat(putResult1.get().getAnnotates()).isEqualTo("Patri M. Mersenno");
  // Log.info(putResult1.get().toString());
  //
  // UUID persName2 = UUID.randomUUID();
  // Position position2 = new Position()//
  // .setXmlId("p-1")//
  // .setOffset(22)//
  // .setLength(17);
  // TextRangeAnnotation textRangeAnnotation2 = new TextRangeAnnotation()//
  // .setId(persName2)//
  // .setName("persName")//
  // .setAnnotator("ckcc")//
  // .setPosition(position2);
  // RestResult<TextRangeAnnotationInfo> putResult2 = client.setResourceTextRangeAnnotation(resourceUUID, textRangeAnnotation2);
  // putResult2.getFailureCause().ifPresent(Log::info);
  // assertThat(putResult2.hasFailed()).isFalse();
  // assertThat(putResult2.get().getAnnotates()).isEqualTo("R. Descartes S.D.");
  // Log.info(putResult2.get().toString());
  //
  // // now, receive all textrangeannotations for this text.
  // RestResult<TextRangeAnnotationList> restResult2 = client.getResourceTextRangeAnnotations(resourceUUID);
  // assertThat(restResult2.hasFailed()).isFalse();
  // TextRangeAnnotationList list2 = restResult.get();
  // assertThat(list2).hasSize(0);
  // }
  //
  // @Test
  // public void testNLA318() {
  // String xml = singleQuotesToDouble("<p xml:id='p-1'>...epouse mad<sup>le</sup> de <sic>Gendrin</sic> soeur du feu archevesque de Sens...</p>");
  // UUID resourceUUID = createResourceWithText(xml);
  // RestResult<Void> result = client.setAnnotator(resourceUUID, "ckcc", new Annotator().setCode("ckcc").setDescription("Co Koccu"));
  // assertRequestSucceeded(result);
  //
  // UUID persName1 = UUID.randomUUID();
  // Position position1 = new Position()//
  // .setXmlId("p-1")//
  // .setOffset(17)//
  // .setLength(10);
  // Map<String, String> attributes = ImmutableMap.of("key", "S0328208");
  // TextRangeAnnotation textRangeAnnotation = new TextRangeAnnotation()//
  // .setId(persName1)//
  // .setName("persName")//
  // .setAnnotator("ckcc")//
  // .setPosition(position1)//
  // .setAttributes(attributes);
  // RestResult<TextRangeAnnotationInfo> putResult1 = client.setResourceTextRangeAnnotation(resourceUUID, textRangeAnnotation);
  // putResult1.getFailureCause().ifPresent(Log::info);
  // assertThat(putResult1.hasFailed()).isFalse();
  // assertThat(putResult1.get().getAnnotates()).isEqualTo("de Gendrin");
  // Log.info(putResult1.get().toString());
  // String dot = client.getTextAsDot(resourceUUID).get();
  // Log.info("dot=\n{}", dot);
  //
  // RestResult<String> textResult = client.getTextAsString(resourceUUID);
  // assertRequestSucceeded(textResult);
  // String xml2 = textResult.get();
  // String expected = "<p xml:id=\"p-1\">...epouse mad<sup>le</sup> <persName key=\"S0328208\" resp=\"#ckcc\">de <sic>Gendrin</sic></persName> soeur du feu archevesque de Sens...</p>";
  // assertThat(xml2).isEqualTo(expected);
  // }
  //
  // @Test
  // public void testNLA318a() {
  // String xml = singleQuotesToDouble("<p xml:id='p-1'>A B <y>de</y> <sic>C</sic> D <x>E</x></p>");
  // UUID resourceUUID = createResourceWithText(xml);
  // RestResult<Void> result = client.setAnnotator(resourceUUID, "ckcc", new Annotator().setCode("ckcc").setDescription("Co Koccu"));
  // assertRequestSucceeded(result);
  // client.getTextAsDot(resourceUUID).get();
  //
  // UUID persName1 = UUID.randomUUID();
  // Position position1 = new Position()//
  // .setXmlId("p-1")//
  // .setOffset(3)//
  // .setLength(6);
  // Map<String, String> attributes = ImmutableMap.of("key", "VALUE");
  // TextRangeAnnotation textRangeAnnotation = new TextRangeAnnotation()//
  // .setId(persName1)//
  // .setName("persName")//
  // .setAnnotator("ckcc")//
  // .setPosition(position1)//
  // .setAttributes(attributes);
  // RestResult<TextRangeAnnotationInfo> putResult1 = client.setResourceTextRangeAnnotation(resourceUUID, textRangeAnnotation);
  // putResult1.getFailureCause().ifPresent(Log::info);
  // assertThat(putResult1.hasFailed()).isFalse();
  // assertThat(putResult1.get().getAnnotates()).isEqualTo("B de C");
  // Log.info(putResult1.get().toString());
  // client.getTextAsDot(resourceUUID).get();
  //
  // RestResult<String> textResult = client.getTextAsString(resourceUUID);
  // assertRequestSucceeded(textResult);
  // String xml2 = textResult.get();
  // String expected = "<p xml:id=\"p-1\">A <persName key=\"VALUE\" resp=\"#ckcc\">B <y>de</y> <sic>C</sic></persName> D <x>E</x></p>";
  // assertThat(xml2).isEqualTo(expected);
  // }
  //
  // @Test
  // public void testNLA312() {
  // String xml = singleQuotesToDouble("<p xml:id='p-1'>Willie Wortel vindt uit.</p>");
  // UUID resourceUUID = createResourceWithText(xml);
  // RestResult<Void> result = client.setAnnotator(resourceUUID, "ckcc", new Annotator().setCode("ckcc").setDescription("Co Koccu"));
  // assertRequestSucceeded(result);
  //
  // UUID persNameUUID = UUID.randomUUID();
  // Position position1 = new Position()//
  // .setXmlId("p-1")//
  // .setOffset(1)//
  // .setLength(13);
  // TextRangeAnnotation textRangeAnnotation = new TextRangeAnnotation()//
  // .setId(persNameUUID)//
  // .setName("persName")//
  // .setAnnotator("ckcc")//
  // .setPosition(position1);
  // RestResult<TextRangeAnnotationInfo> putResult1 = client.setResourceTextRangeAnnotation(resourceUUID, textRangeAnnotation);
  // putResult1.getFailureCause().ifPresent(Log::info);
  // assertThat(putResult1.hasFailed()).isFalse();
  // assertThat(putResult1.get().getAnnotates()).isEqualTo("Willie Wortel");
  // Log.info(putResult1.get().toString());
  //
  // UUID persIdUUID = UUID.randomUUID();
  // Position position2 = new Position()//
  // .setTargetAnnotationId(persNameUUID);
  // Map<String, String> attributes = ImmutableMap.of("id", "W. Wortel (1934-)");
  // TextRangeAnnotation textRangeAnnotation2 = new TextRangeAnnotation()//
  // .setId(persIdUUID)//
  // .setName("persName_id")//
  // .setAnnotator("ckcc")//
  // .setPosition(position2)//
  // .setAttributes(attributes);
  // RestResult<TextRangeAnnotationInfo> putResult2 = client.setResourceTextRangeAnnotation(resourceUUID, textRangeAnnotation2);
  // putResult2.getFailureCause().ifPresent(Log::info);
  // assertThat(putResult2.hasFailed()).isFalse();
  // // assertThat(putResult2.get().getAnnotates()).isEqualTo("Willie Wortel");
  // Log.info(putResult2.get().toString());
  //
  // RestResult<String> textResult = client.getTextAsString(resourceUUID);
  // assertRequestSucceeded(textResult);
  // String xml2 = textResult.get();
  // String expected = singleQuotesToDouble("<p xml:id='p-1'><persName resp='#ckcc'><persName_id id='W. Wortel (1934-)' resp='#ckcc'>Willie Wortel</persName_id></persName> vindt uit.</p>");
  // assertThat(xml2).isEqualTo(expected);
  // }
  //
  // @Test
  // public void testNLA307() {
  // String xml = //
  // singleQuotesToDouble("<text>\n"//
  // + "<p xml:id='p-1'>prologue</p>\n"//
  // + "<p xml:id='p-2'>hello</p>\n"//
  // + "<p xml:id='p-3'>goodbye</p>\n"//
  // + "<p xml:id='p-4'>epilogue</p>\n"//
  // + "</text>");
  //
  // UUID resourceUUID = createResourceWithText(xml);
  // Log.info("xml 0: {}", xml);
  // Annotator annotator = new Annotator().setCode("ckcc").setDescription("something");
  // client.setAnnotator(resourceUUID, "ckcc", annotator);
  //
  // // set first annotation (bad value, should be "opener"
  // UUID annotationUUID1 = UUID.randomUUID();
  // Position position1 = new Position()//
  // .setXmlId("p-2");
  // Map<String, String> badProperties = ImmutableMap.of("value", "closer");
  // TextRangeAnnotation textRangeAnnotation = new TextRangeAnnotation()//
  // .setId(annotationUUID1)//
  // .setName("p_type")//
  // .setAnnotator("ckcc")//
  // .setPosition(position1)//
  // .setAttributes(badProperties);
  //
  // String xmlOut = applyAnnotation(resourceUUID, textRangeAnnotation);
  // String expectation = //
  // singleQuotesToDouble("<text>\n"//
  // + "<p xml:id='p-1'>prologue</p>\n"//
  // + "<p xml:id='p-2'><p_type value='closer' resp='#ckcc'>hello</p_type></p>\n"//
  // + "<p xml:id='p-3'>goodbye</p>\n"//
  // + "<p xml:id='p-4'>epilogue</p>\n"//
  // + "</text>");
  // assertThat(xmlOut).isEqualTo(expectation);
  //
  // // correct first annotation
  // Map<String, String> goodProperties = ImmutableMap.of("value", "opener");
  // TextRangeAnnotation textRangeAnnotation2 = new TextRangeAnnotation()//
  // .setId(annotationUUID1)//
  // .setName("p_type")//
  // .setAnnotator("ckcc")//
  // .setPosition(position1)//
  // .setAttributes(goodProperties);
  //
  // xmlOut = applyAnnotation(resourceUUID, textRangeAnnotation2);
  // expectation = //
  // singleQuotesToDouble("<text>\n"//
  // + "<p xml:id='p-1'>prologue</p>\n"//
  // + "<p xml:id='p-2'><p_type value='opener' resp='#ckcc'>hello</p_type></p>\n"//
  // + "<p xml:id='p-3'>goodbye</p>\n"//
  // + "<p xml:id='p-4'>epilogue</p>\n"//
  // + "</text>");
  // assertThat(xmlOut).isEqualTo(expectation);
  //
  // // set second annotation
  // UUID annotationUUID2 = UUID.randomUUID();
  // Position position2 = new Position()//
  // .setXmlId("p-3");
  // Map<String, String> properties = ImmutableMap.of("value", "closer");
  // TextRangeAnnotation textRangeAnnotation3 = new TextRangeAnnotation()//
  // .setId(annotationUUID2)//
  // .setName("p_type")//
  // .setAnnotator("ckcc")//
  // .setPosition(position2)//
  // .setAttributes(properties);
  //
  // xmlOut = applyAnnotation(resourceUUID, textRangeAnnotation3);
  // expectation = //
  // singleQuotesToDouble("<text>\n"//
  // + "<p xml:id='p-1'>prologue</p>\n"//
  // + "<p xml:id='p-2'><p_type value='opener' resp='#ckcc'>hello</p_type></p>\n"//
  // + "<p xml:id='p-3'><p_type value='closer' resp='#ckcc'>goodbye</p_type></p>\n"//
  // + "<p xml:id='p-4'>epilogue</p>\n"//
  // + "</text>");
  // assertThat(xmlOut).isEqualTo(expectation);
  // }
  //
  // private String applyAnnotation(UUID resourceUUID, TextRangeAnnotation textRangeAnnotation) {
  // RestResult<TextRangeAnnotationInfo> putResult2 = client.setResourceTextRangeAnnotation(resourceUUID, textRangeAnnotation);
  // putResult2.getFailureCause().ifPresent(Log::info);
  // assertThat(putResult2.hasFailed()).isFalse();
  //
  // RestResult<String> textResult = client.getTextAsString(resourceUUID);
  // assertRequestSucceeded(textResult);
  // return textResult.get();
  // }
  //
  // private UUID createResourceWithText(String xml) {
  // String resourceRef = "test";
  // UUID resourceUUID = createResource(resourceRef);
  // TextImportStatus textGraphImportStatus = setResourceText(resourceUUID, xml);
  // URI expectedURI = URI.create("http://localhost:2016/resources/" + resourceUUID + "/text/xml");
  // assertThat(textGraphImportStatus.getTextURI()).isEqualTo(expectedURI);
  // return resourceUUID;
  // }
  //
}
