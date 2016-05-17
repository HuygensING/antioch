package nl.knaw.huygens.alexandria.text;

/*
 * #%L
 * alexandria-main
 * =======
 * Copyright (C) 2015 - 2016 Huygens ING (KNAW)
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

import java.util.List;

import org.junit.Test;

public class XmlIdVisitorTest extends AlexandriaVisitorTest {
  @Test
  public void testVisitor1() {
    String xml = singleQuotesToDouble("<text>"//
        + "<p xml:id='p-1'>par 1</p>"//
        + "<p>par <num xml:id='num2'>2</num></p>"//
        + "</text>");

    XmlIdVisitor idVisitor = new XmlIdVisitor();

    visitXml(xml, idVisitor);

    List<String> existingIds = idVisitor.getXmlIds();
    assertThat(existingIds).containsExactly("p-1", "num2");
  }

  @Test
  public void testVisitor2() {
    String xml = singleQuotesToDouble("<text>"//
        + "<p xml:id='p1'>par 1</p>"//
        + "<p>par <num xml:id='num2'>2</num></p>"//
        + "</text>");

    XmlIdVisitor idVisitor = new XmlIdVisitor();

    visitXml(xml, idVisitor);

    List<String> existingIds = idVisitor.getXmlIds();
    assertThat(existingIds).containsExactly("p1", "num2");
  }

}
