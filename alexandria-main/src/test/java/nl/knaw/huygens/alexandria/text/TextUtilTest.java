package nl.knaw.huygens.alexandria.text;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import nl.knaw.huygens.alexandria.model.BaseLayerDefinition;
import nl.knaw.huygens.alexandria.model.BaseLayerDefinition.BaseElementDefinition;

public class TextUtilTest {
  @Test
  public void testBaseLayerExtraction() {
    // given
    String xml = "<text><div xml:id='div-1' lang='nl'><p xml:id='p01' rend='red'>paragraph with <b><i rend='yes'>text</i></b></p><p>wa</p></div></text>".replace("'", "\"");
    String expected = "<text><div xml:id='div-1'><p xml:id='p01'>paragraph with text</p><p>wa</p></div></text>".replace("'", "\"");
    BaseLayerDefinition def = BaseLayerDefinition//
        .withBaseElements(//
            BaseElementDefinition.withName("text"), //
            BaseElementDefinition.withName("div").withAttributes("xml:id"), //
            BaseElementDefinition.withName("p").withAttributes("xml:id") //
    );

    // when
    String baseLayer = TextUtil.extractBaseLayer(xml, def);
    
    // then expect
    assertThat(baseLayer).isEqualTo(expected);
  }
}
