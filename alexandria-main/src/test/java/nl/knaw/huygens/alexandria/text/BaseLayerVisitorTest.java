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

import java.util.Map;

import org.junit.Test;

import nl.knaw.huygens.alexandria.api.model.BaseElementDefinition;
import nl.knaw.huygens.alexandria.api.model.BaseLayerDefinition;

public class BaseLayerVisitorTest extends AlexandriaVisitorTest {
  @Test
  public void testVisitorForXmlWithSubtextPlaceholders() {
    String xml = singleQuotesToDouble("<text xml:id='text-1'>"//
        + "<p xml:id='p-1'>123<alexandria_subtextplaceholder xml:id='alexandria:subtext-1'/></p>"//
        + "<p xml:id='p-2'>4567<alexandria_subtextplaceholder xml:id='alexandria:subtext-2'/> 9012345ô</p>"//
        + "<alexandria_subtextplaceholder xml:id='alexandria:subtext-3'/>"//
        + "</text>");
    String expectedBase = singleQuotesToDouble("<text xml:id='text-1'>"//
        + "<p xml:id='p-1'>123</p>"//
        + "<p xml:id='p-2'>4567 9012345ô</p>"//
        + "</text>");

    BaseLayerDefinition definition = BaseLayerDefinition.withBaseElements(//
        BaseElementDefinition.withName("text"), //
        BaseElementDefinition.withName("p")//
    );
    BLVContext context = new BLVContext();
    BaseLayerVisitor visitor = new BaseLayerVisitor(context, definition, "");

    visitXml(xml, visitor);

    softly.assertThat(visitor.getContext().getResult()).isEqualTo(expectedBase);

    BaseLayerData baseLayerData = visitor.getBaseLayerData();
    softly.assertThat(baseLayerData).isNotNull();

    Map<String, String> subresourceXPathMap = context.getSubresourceXPathMap();
    softly.assertThat(subresourceXPathMap).as("subresourceXPathMap.size").hasSize(3);
    softly.assertThat(subresourceXPathMap).as("subresourceXPathMap[0]").containsEntry("alexandria:subtext-1", "substring(/,4,0)");
    softly.assertThat(subresourceXPathMap).as("subresourceXPathMap[1]").containsEntry("alexandria:subtext-2", "substring(/,8,0)");
    softly.assertThat(subresourceXPathMap).as("subresourceXPathMap[2]").containsEntry("alexandria:subtext-3", "substring(/,17,0)");
  }
}
