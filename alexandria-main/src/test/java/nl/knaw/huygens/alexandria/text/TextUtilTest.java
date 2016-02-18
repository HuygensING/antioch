package nl.knaw.huygens.alexandria.text;

import static java.util.stream.Collectors.joining;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import nl.knaw.huygens.Log;
import nl.knaw.huygens.alexandria.model.BaseLayerDefinition;
import nl.knaw.huygens.alexandria.model.BaseLayerDefinition.BaseElementDefinition;

public class TextUtilTest {
  @Test
  public void testBaseLayerExtraction() {
    // given
    String xml = ("<text>"//
        + "<div xml:id='div-1' lang='nl'>"//
        + "<p xml:id='p01' rend='red'>paragraph with <b><i rend='yes'>text</i></b></p>"//
        + "<p>two</p>"//
        + "</div>"//
        + "<div xml:id='div-2' lang='nl'>"//
        + "<p>three</p>"//
        + "</div>"//
        + "</text>").replace("'", "\"");
    String expected = ("<text>"//
        + "<div xml:id='div-1'>"//
        + "<p xml:id='p01'>paragraph with text</p>"//
        + "<p>two</p>"//
        + "</div>"//
        + "<div xml:id='div-2'>"//
        + "<p>three</p>"//
        + "</div>"//
        + "</text>").replace("'", "\"");
    BaseLayerDefinition def = BaseLayerDefinition//
        .withBaseElements(//
            BaseElementDefinition.withName("text"), //
            BaseElementDefinition.withName("div").withAttributes("xml:id"), //
            BaseElementDefinition.withName("p").withAttributes("xml:id") //
    );

    // when
    BaseLayerData baseLayerData = TextUtil.extractBaseLayerData(xml, def);
    Log.info("AnnotationData =\n{}", baseLayerData.getAnnotationData().stream().map(AnnotationData::toVerbose).collect(joining("\n")));
    String baseLayer = baseLayerData.getBaseLayer();

    // then expect
    assertThat(baseLayer).isEqualTo(expected);
  }
}
