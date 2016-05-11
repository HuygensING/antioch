package nl.knaw.huygens.alexandria.client;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import nl.knaw.huygens.alexandria.api.model.ElementView.ElementMode;
import nl.knaw.huygens.alexandria.api.model.ElementViewDefinition;
import nl.knaw.huygens.alexandria.api.model.TextViewDefinition;

public class ResourceTextViewTest extends AlexandriaClientTest {
  @Before
  public void before() {
    client.setAuthKey(AUTHKEY);
    client.setAutoConfirm(true);
  }

  @Test
  public void testSettingResourceViewWorks() {
    UUID resourceId = createResource("resourceWithXML");

    String xml = singleQuotesToDouble(//
        "<text>"//
            + "<p>A</p>"//
            + "<p>B<note>Borrel</note></p>"//
            + "</text>"//
    );
    String expectedFilteredXml = singleQuotesToDouble(//
        "<text>"//
            + "<p>A</p>"//
            + "<p>B</p>"//
            + "</text>"//
    );
    setResourceText(resourceId, xml);

    String textViewName = "view0";
    TextViewDefinition textView = new TextViewDefinition().setDescription("My View");
    textView.getElementViewDefinitions().put("note", new ElementViewDefinition().setElementMode(ElementMode.hide));
    RestResult<URI> result = client.setResourceTextView(resourceId, textViewName, textView);
    assertRequestSucceeded(result);

    // get the view on the resourcetext
    RestResult<String> xmlViewResult = client.getTextAsString(resourceId, textViewName);
    assertRequestSucceeded(xmlViewResult);
    String xmlView = xmlViewResult.get();
    assertThat(xmlView).isEqualTo(expectedFilteredXml);
  }

}
