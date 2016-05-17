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

import com.google.common.collect.ImmutableList;

public class AddMissingIdVisitorTest extends AlexandriaVisitorTest {
  @Test
  public void testVisitor() {
    String xml = singleQuotesToDouble("<text>"//
        + "<p xml:id='p-1'>par 1</p>"//
        + "<p>par <num>2</num></p>"//
        + "</text>");
    String expected = singleQuotesToDouble("<text xml:id='text-1'>"//
        + "<p xml:id='p-1'>par 1</p>"//
        + "<p xml:id='p-2'>par <num>2</num></p>"//
        + "</text>");
    List<String> existingBaseElementIds = ImmutableList.of("p-1");
    List<String> baseElementNames = ImmutableList.of("text", "p");
    AddMissingIdVisitor addIdVisitor = new AddMissingIdVisitor(existingBaseElementIds, baseElementNames);

    visitXml(xml, addIdVisitor);

    String xmlWithIds = addIdVisitor.getContext().getResult();
    assertThat(xmlWithIds).isEqualTo(expected);
  }

}
