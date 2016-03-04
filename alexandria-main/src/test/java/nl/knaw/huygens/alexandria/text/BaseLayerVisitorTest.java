package nl.knaw.huygens.alexandria.text;

import java.util.Map;

import org.junit.Test;

import nl.knaw.huygens.alexandria.api.model.BaseElementDefinition;
import nl.knaw.huygens.alexandria.api.model.BaseLayerDefinition;

public class BaseLayerVisitorTest extends AlexandriaVisitorTest {
  @Test
  public void testVisitorForXmlWithSubtextPlaceholders() {
    String xml = singleQuotesToDouble("<text xml:id='text-1'>"//
        + "<p xml:id='p-1'>foo<alexandria_subtextplaceholder xml:id='alexandria:subtext-1'/></p>"//
        + "<p xml:id='p-2'>babar<alexandria_subtextplaceholder xml:id='alexandria:subtext-2'/></p>"//
        + "</text>");
    String expectedBase = singleQuotesToDouble("<text xml:id='text-1'>"//
        + "<p xml:id='p-1'>foo</p>"//
        + "<p xml:id='p-2'>babar</p>"//
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
    softly.assertThat(subresourceXPathMap).containsEntry("alexandria:subtext-1", "substring(//p[@xml:id='p-1'],4,0)");
    softly.assertThat(subresourceXPathMap).containsEntry("alexandria:subtext-2", "substring(//p[@xml:id='p-2'],6,0)");
  }
}
