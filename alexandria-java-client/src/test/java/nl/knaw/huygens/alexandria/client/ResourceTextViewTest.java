package nl.knaw.huygens.alexandria.client;

public class ResourceTextViewTest extends AlexandriaClientTest {
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
