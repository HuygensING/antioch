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

public class ResourceTextViewTest extends AntiochClientTest {
  // TODO move to markup module
  // @Before
  // public void before() {
  // client.setAuthKey(AUTHKEY);
  // client.setAutoConfirm(true);
  // }
  //
  // @Test
  // public void testSettingResourceViewWorks() {
  // UUID resourceId = createResource("resourceWithXML");
  //
  // String xml = singleQuotesToDouble(//
  // "<text>"//
  // + "<p>Aap <i>Noot</i> Mies</p>"//
  // + "<p>B<note>Borrel</note></p>"//
  // + "</text>"//
  // );
  // String expectedFilteredXml = singleQuotesToDouble(//
  // "<text>"//
  // + "<p>Aap Noot Mies</p>"//
  // + "<p>B</p>"//
  // + "</text>"//
  // );
  // setResourceText(resourceId, xml);
  //
  // String textViewName = "view0";
  // TextViewDefinition textView = new TextViewDefinition().setDescription("My View");
  // textView.setElementViewDefinition("note", new ElementViewDefinition().setElementMode(ElementMode.hide));
  // textView.setElementViewDefinition("i", new ElementViewDefinition().setElementMode(ElementMode.hideTag));
  // RestResult<Void> result = client.setResourceTextView(resourceId, textViewName, textView);
  // assertRequestSucceeded(result);
  //
  // // get the view on the resourcetext
  // RestResult<String> xmlViewResult = client.getTextAsString(resourceId, textViewName);
  // assertRequestSucceeded(xmlViewResult);
  // String xmlView = xmlViewResult.get();
  // assertThat(xmlView).isEqualTo(expectedFilteredXml);
  // }
  //
}
