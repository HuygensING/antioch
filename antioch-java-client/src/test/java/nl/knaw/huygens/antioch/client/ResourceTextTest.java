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

public class ResourceTextTest extends AntiochClientTest {
  // TODO move to markup module
  // @Before
  // public void before() {
  // client.setAuthKey(AUTHKEY);
  // client.setAutoConfirm(true);
  // }
  //
  // @Test
  // public void testResourceText() {
  // String resourceRef = "test";
  // UUID resourceUuid = createResource(resourceRef);
  // String xml = "<text>Something</text>";
  // TextImportStatus textGraphImportStatus = setResourceText(resourceUuid, xml);
  // URI expectedURI = URI.create("http://localhost:2016/resources/" + resourceUuid + "/text/xml");
  // assertThat(textGraphImportStatus.getTextURI()).isEqualTo(expectedURI);
  //
  // RestResult<TextEntity> textInfoResult = client.getTextInfo(resourceUuid);
  // assertRequestSucceeded(textInfoResult);
  // TextEntity textEntity = textInfoResult.get();
  // assertThat(textEntity.getTextViews()).isEmpty();
  // assertThat(textEntity.getXmlURI()).isEqualTo(expectedURI);
  //
  // RestResult<String> xmlReadResult = client.getTextAsString(resourceUuid);
  // assertRequestSucceeded(xmlReadResult);
  // String xml2 = xmlReadResult.get();
  // assertThat(xml2).isEqualTo(xml);
  //
  // RestResult<String> dotReadResult = client.getTextAsDot(resourceUuid);
  // assertRequestSucceeded(dotReadResult);
  // String dot = dotReadResult.get();
  // String expectedDot = singleQuotesToDouble("digraph TextGraph {\n"//
  // + " ranksep=1.0\n\n"//
  // + " t0 [shape=box, label='Something'];\n"//
  // + " a0 [label='text'];\n"//
  // + " a0 -> t0 [color='blue'];\n\n"//
  // + " {rank=same;t0;}\n"//
  // + " {rank=same;a0;}\n"//
  // + "}");
  // assertThat(dot).isEqualTo(expectedDot);
  // }
  //
  // @Test
  // public void testMilestoneHandling() {
  // UUID resourceUuid = createResource("test");
  // String xml = singleQuotesToDouble("<text><pb n='1' xml:id='pb-1'/><p><figure><graphic url='beec002jour04ill02.gif'/></figure></p></text>");
  // TextImportStatus textGraphImportStatus = setResourceText(resourceUuid, xml);
  // URI expectedURI = URI.create("http://localhost:2016/resources/" + resourceUuid + "/text/xml");
  // assertThat(textGraphImportStatus.getTextURI()).isEqualTo(expectedURI);
  //
  // RestResult<String> xmlReadResult = client.getTextAsString(resourceUuid);
  // assertRequestSucceeded(xmlReadResult);
  // String xml2 = xmlReadResult.get();
  // assertThat(xml2).isEqualTo(xml);
  // }
}
