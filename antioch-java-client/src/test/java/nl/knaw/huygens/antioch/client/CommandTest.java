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

public class CommandTest extends AntiochClientTest {

  // TODO move to markup module
  // @Before
  // public void before() {
  // client.setAuthKey(AUTHKEY);
  // client.setAutoConfirm(true);
  // }
  //
  // @Test
  // public void testAddUniqueIdCommmandWorks() {
  // UUID resourceUuid = createResource("xml");
  // String xml = "<text><p>Alinea 1</p><p>Alinea 2</p></text>";
  // String expectedXml = singleQuotesToDouble("<text xml:id='text-1'><p xml:id='p-1'>Alinea 1</p><p xml:id='p-2'>Alinea 2</p></text>");
  // setResourceText(resourceUuid, xml);
  // Map<String, Object> parameters = ImmutableMap.<String, Object> builder()//
  // .put("resourceIds", ImmutableList.of(resourceUuid))//
  // .put("elements", ImmutableList.of("text", "p"))//
  // .build();
  // RestResult<CommandResponse> result = client.doCommand(Commands.ADD_UNIQUE_ID, parameters);
  // assertRequestSucceeded(result);
  // CommandResponse commandResponse = result.get();
  // assertThat(commandResponse.success()).isTrue();
  //
  // RestResult<String> xmlReadResult = client.getTextAsString(resourceUuid);
  // assertRequestSucceeded(xmlReadResult);
  // String xml2 = xmlReadResult.get();
  // assertThat(xml2).isEqualTo(expectedXml);
  // }
  //
  // @Test
  // public void testXpathCommmandWorks() {
  // UUID resourceUuid = createResource("xml");
  // String xml = "<text><p>Alinea 1</p><p>Alinea 2</p></text>";
  // String expected = "Alinea 2";
  // setResourceText(resourceUuid, xml);
  // Map<String, Object> parameters = ImmutableMap.<String, Object> builder()//
  // .put("resourceIds", ImmutableList.of(resourceUuid))//
  // .put("xpath", "string(/text/p[2])")//
  // .build();
  // RestResult<CommandResponse> result = client.doCommand(Commands.XPATH, parameters);
  // assertRequestSucceeded(result);
  // CommandResponse commandResponse = result.get();
  // assertThat(commandResponse.success()).isTrue();
  // Map<String, Map<String, String>> result2 = (Map<String, Map<String, String>>) commandResponse.getResult();
  // Map<String, String> map = result2.get(resourceUuid.toString());
  // XPathResult returnedXPathResult = new XPathResult(XPathResult.Type.valueOf(map.get("type")), map.get("result"));
  // XPathResult expectedXPathResult = new XPathResult(XPathResult.Type.STRING, expected);
  // assertThat(returnedXPathResult).isEqualToComparingFieldByField(expectedXPathResult);
  // }
  //
  // @Test
  // public void testAQL2CommmandWorks() throws InterruptedException {
  // UUID resourceUuid = createResource("xml");
  // String xml = "<text><p>Alinea 1</p><p>Alinea 2</p></text>";
  // setResourceText(resourceUuid, xml);
  // Map<String, Object> parameters = ImmutableMap.<String, Object> builder()//
  // .put("command", "bye(\"world\")")//
  // .build();
  // RestResult<CommandResponse> result = client.doCommand(Commands.AQL2, parameters);
  // assertRequestSucceeded(result);
  //
  // CommandResponse commandResponse = result.get();
  // UUID statusId = commandResponse.getStatusId();
  // Log.info("statusId={}", statusId);
  //
  // boolean done = false;
  // RestResult<CommandStatus> statusResult = null;
  // while (!done) {
  // Thread.sleep(1000);
  // statusResult = client.getCommandStatus(Commands.AQL2, statusId);
  // assertRequestSucceeded(statusResult);
  // done = statusResult.get().isDone();
  // }
  // Object result2 = statusResult.get().getResult();
  // Log.info("{}", result2);
  // assertThat(result2).isEqualTo("Goodbye world!");
  // }
  //
  // // @Test
  // public void testAnnotationCommandWorks() {
  // UUID resourceUuid = createResource("xml");
  // String xml = "<root>"//
  // + "<p xml:id=\"p-1\">"//
  // + "<seg xml:id=\"seg-1\">Some</seg>"//
  // + " text</p>"//
  // + "</root>";
  // // String expectedXml = "<root>"//
  // // + "<p xml:id=\"p-1\"><annotation xml:lang=\"?\">"//
  // // + "<seg xml:id=\"seg-1\">Some</seg>"//
  // // + " text</annotation></p>"//
  // // + "</root>";
  // String expectedXml = "<root>"//
  // + "<p xml:id=\"p-1\"><annotation xml:lang=\"?\">"//
  // + "<seg xml:id=\"seg-1\"><annotation xml:lang=\"fr\">Some</annotation></seg>"//
  // + " text</annotation></p>"//
  // + "</root>";
  // setResourceText(resourceUuid, xml);
  //
  // Map<String, String> annotations = ImmutableMap.of(//
  // "seg-1", "fr", //
  // "p-1", "?");
  //
  // for (Map.Entry<String, String> entry : annotations.entrySet()) {
  // Map<String, Object> attributes = ImmutableMap.<String, Object> builder()//
  // .put("xml:lang", entry.getValue())//
  // .build();
  // Map<String, Object> element = ImmutableMap.<String, Object> builder()//
  // .put("name", "annotation")//
  // .put("attributes", attributes)//
  // .build();
  // Map<String, Object> parameters = ImmutableMap.<String, Object> builder()//
  // .put("resourceIds", ImmutableList.of(resourceUuid))//
  // .put("xmlIds", ImmutableList.of(entry.getKey()))//
  // .put("element", element)//
  // .build();
  // RestResult<CommandResponse> result = client.doCommand(Commands.WRAP_CONTENT_IN_ELEMENT, parameters);
  // assertRequestSucceeded(result);
  // CommandResponse commandResponse = result.get();
  // assertThat(commandResponse.success()).isTrue();
  // }
  //
  // RestResult<String> xmlReadResult = client.getTextAsString(resourceUuid);
  // assertRequestSucceeded(xmlReadResult);
  // String xml2 = xmlReadResult.get();
  // assertThat(xml2).isEqualTo(expectedXml);
  // }
  //
  // // @Test
  // public void testAnnotationCommandWorks2() {
  // UUID resourceUuid = createResource("xml");
  // String xml = "<p xml:id=\"p-10\">Tuus\n"//
  // + "<lb/><persName key=\"beeckman.isaac.1588-1637\" resp=\"#ckcc\">Isacus Beeckmannus</persName>"//
  // + "</p>";
  // String expectedXml = "<p xml:id=\"p-10\"><p_lang value=\"la\" resp=\"#ckcc\"><p_type value=\"closer\" resp=\"#ckcc\">Tuus\n"//
  // + "<lb/><persName key=\"beeckman.isaac.1588-1637\" resp=\"#ckcc\">Isacus Beeckmannus</persName>"//
  // + "</p_type></p_lang></p>";
  // setResourceText(resourceUuid, xml);
  //
  // annotate(resourceUuid, element("p_type", attributes("closer")));
  // annotate(resourceUuid, element("p_lang", attributes("la")));
  //
  // RestResult<String> xmlReadResult = client.getTextAsString(resourceUuid);
  // assertRequestSucceeded(xmlReadResult);
  // String xml2 = xmlReadResult.get();
  // assertThat(xml2).isEqualTo(expectedXml);
  // }
  //
  // private void annotate(UUID resourceUuid, Map<String, Object> element) {
  // Map<String, Object> parameters1 = ImmutableMap.<String, Object> builder()//
  // .put("resourceIds", ImmutableList.of(resourceUuid))//
  // .put("xmlIds", ImmutableList.of("p-10"))//
  // .put("element", element)//
  // .build();
  // RestResult<CommandResponse> result = client.doCommand(Commands.WRAP_CONTENT_IN_ELEMENT, parameters1);
  // assertRequestSucceeded(result);
  // CommandResponse commandResponse = result.get();
  // assertThat(commandResponse.success()).isTrue();
  // }
  //
  // private Map<String, Object> attributes(String value) {
  // return ImmutableMap.<String, Object> builder()//
  // .put("value", value)//
  // .put("resp", "#ckcc")//
  // .build();
  // }
  //
  // private Map<String, Object> element(String name, Map<String, Object> attributes) {
  // return ImmutableMap.<String, Object> builder()//
  // .put("name", name)//
  // .put("attributes", attributes)//
  // .build();
  // }

}
