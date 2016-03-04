package nl.knaw.huygens.alexandria.text;

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
    BaseLayerVisitor visitor = new BaseLayerVisitor(definition);

    visitXml(xml, visitor);

    softly.assertThat(visitor.getContext().getResult()).isEqualTo(expectedBase);

    BaseLayerData baseLayerData = visitor.getBaseLayerData();
    softly.assertThat(baseLayerData).isNotNull();

    Map<String, String> subresourceXPathMap = visitor.getSubresourceXPathMap();
    softly.assertThat(subresourceXPathMap).as("subresourceXPathMap.size").hasSize(3);
    softly.assertThat(subresourceXPathMap).as("subresourceXPathMap[0]").containsEntry("alexandria:subtext-1", "substring(/,4,0)");
    softly.assertThat(subresourceXPathMap).as("subresourceXPathMap[1]").containsEntry("alexandria:subtext-2", "substring(/,8,0)");
    softly.assertThat(subresourceXPathMap).as("subresourceXPathMap[2]").containsEntry("alexandria:subtext-3", "substring(/,17,0)");
  }
}
