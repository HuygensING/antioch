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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import nl.knaw.huygens.alexandria.api.model.text.view.ElementView.ElementMode;
import nl.knaw.huygens.alexandria.api.model.text.view.ElementViewDefinition;
import nl.knaw.huygens.alexandria.api.model.text.view.TextViewDefinition;

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
            + "<p>Aap <i>Noot</i> Mies</p>"//
            + "<p>B<note>Borrel</note></p>"//
            + "</text>"//
    );
    String expectedFilteredXml = singleQuotesToDouble(//
        "<text>"//
            + "<p>Aap Noot Mies</p>"//
            + "<p>B</p>"//
            + "</text>"//
    );
    setResourceText(resourceId, xml);

    String textViewName = "view0";
    TextViewDefinition textView = new TextViewDefinition().setDescription("My View");
    textView.setElementViewDefinition("note", new ElementViewDefinition().setElementMode(ElementMode.hide));
    textView.setElementViewDefinition("i", new ElementViewDefinition().setElementMode(ElementMode.hideTag));
    RestResult<Void> result = client.setResourceTextView(resourceId, textViewName, textView);
    assertRequestSucceeded(result);

    // get the view on the resourcetext
    RestResult<String> xmlViewResult = client.getTextAsString(resourceId, textViewName);
    assertRequestSucceeded(xmlViewResult);
    String xmlView = xmlViewResult.get();
    assertThat(xmlView).isEqualTo(expectedFilteredXml);
  }

}
