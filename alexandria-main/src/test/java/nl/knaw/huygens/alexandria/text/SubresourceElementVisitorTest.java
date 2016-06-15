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

import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.google.common.collect.ImmutableList;

public class SubresourceElementVisitorTest extends AlexandriaVisitorTest {

  @Test
  public void testVisitor() {
    // given
    String xml = singleQuotesToDouble("<text xml:id='text-1'>"//
        + "<p xml:id='p-1'>foo<note n='1'>probably by <persName>someone</persName></note></p>"//
        + "<p xml:id='p-2'>bar<comment n='2'>most likely by <persName>someone else</persName></comment></p>"//
        + "</text>");
    String expectedBase = singleQuotesToDouble("<text xml:id='text-1'>"//
        + "<p xml:id='p-1'>foo<alexandria_subtextplaceholder xml:id='alexandria:subtext-1'/></p>"//
        + "<p xml:id='p-2'>bar<alexandria_subtextplaceholder xml:id='alexandria:subtext-2'/></p>"//
        + "</text>");
    String expectedSubText1 = singleQuotesToDouble("<note n='1'>probably by <persName>someone</persName></note>");
    String expectedSubText2 = singleQuotesToDouble("<comment n='2'>most likely by <persName>someone else</persName></comment>");
    List<String> subresourceElements = ImmutableList.of("note", "comment");
    SEVContext context = new SEVContext();
    SubresourceElementVisitor subresourceVisitor = new SubresourceElementVisitor(context, subresourceElements);

    // when
    visitXml(xml, subresourceVisitor);

    // expect
    softly.assertThat(context.getBaseText()).isEqualTo(expectedBase);
    Map<String, String> subresourceTexts = context.getSubresourceTexts();
    softly.assertThat(subresourceTexts).hasSize(2);
    softly.assertThat(subresourceTexts).containsEntry("alexandria:subtext-1", expectedSubText1);
    softly.assertThat(subresourceTexts).containsEntry("alexandria:subtext-2", expectedSubText2);
  }

  @Test
  public void testVisitorWithNestedSubresourceElements() {
    // given
    String xml = singleQuotesToDouble("<text xml:id='text-1'>"//
        + "<p xml:id='p-1'>foo<note n='1'>"//
        + "probably by <persName>someone</persName>"//
        + "<note n='2'>most likely by <persName>someone else</persName></note>"//
        + "</note></p>"//
        + "</text>");
    String expectedBase = singleQuotesToDouble("<text xml:id='text-1'>"//
        + "<p xml:id='p-1'>foo<alexandria_subtextplaceholder xml:id='alexandria:subtext-1'/></p>"//
        + "</text>");
    String expectedSubText = singleQuotesToDouble("<note n='1'>"//
        + "probably by <persName>someone</persName>"//
        + "<note n='2'>most likely by <persName>someone else</persName></note>"//
        + "</note>");
    List<String> subresourceElements = ImmutableList.of("note");
    SEVContext context = new SEVContext();
    SubresourceElementVisitor subresourceVisitor = new SubresourceElementVisitor(context, subresourceElements);

    // when
    visitXml(xml, subresourceVisitor);

    // expect
    softly.assertThat(context.getBaseText()).isEqualTo(expectedBase);
    Map<String, String> subresourceTexts = context.getSubresourceTexts();
    softly.assertThat(subresourceTexts).hasSize(1);
    softly.assertThat(subresourceTexts).containsEntry("alexandria:subtext-1", expectedSubText);
  }

}
