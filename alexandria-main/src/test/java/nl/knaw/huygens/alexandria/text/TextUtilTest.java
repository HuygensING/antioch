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
        + "<p xml:id='p1' rend='red'>paragraph with <b><i rend='yes'>text</i></b></p>"//
        + "<p>two</p>"//
        + "</div>"//
        + "<div xml:id='div-2' lang='nl'>"//
        + "<p>three</p>"//
        + "</div>"//
        + "</text>").replace("'", "\"");
    String expected = ("<text xml:id='text-1'>"//
        + "<div xml:id='div-1'>"//
        + "<p xml:id='p1'>paragraph with text</p>"//
        + "<p xml:id='p-1'>two</p>"//
        + "</div>"//
        + "<div xml:id='div-2'>"//
        + "<p xml:id='p-2'>three</p>"//
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
    assertThat(baseLayerData.validationFailed()).isFalse();
    assertThat(baseLayer).isEqualTo(expected);
    Log.info(expected);
  }

  @Test
  public void testBaseLayerExtractionFailsOnRootElementNotInBaseLayerDefinition() {
    // given
    String xml = ("<text>"//
        + "<div xml:id='div-1' lang='nl'>"//
        + "<p xml:id='p1' rend='red'>paragraph with <b><i rend='yes'>text</i></b></p>"//
        + "</div>"//
        + "</text>").replace("'", "\"");
    BaseLayerDefinition def = BaseLayerDefinition//
        .withBaseElements(//
            BaseElementDefinition.withName("div").withAttributes("xml:id"), //
            BaseElementDefinition.withName("p").withAttributes("xml:id") //
    );

    // when
    BaseLayerData baseLayerData = TextUtil.extractBaseLayerData(xml, def);

    // then expect
    assertThat(baseLayerData.validationFailed()).isTrue();
    assertThat(baseLayerData.getValidationErrors()).contains("Validation error: root element <text> is not in the base layer definition.");
  }

  @Test
  public void testBaseLayerExtractionAddsMissingXmlIdsToBaseElements() {
    // given
    String xml = ("<text>"//
        + "<div xml:id='div-1'>"//
        + "<p xml:id='p1'>par <num>1</num></p>"//
        + "<p>par 2</p>"//
        + "</div>"//
        + "</text>").replace("'", "\"");
    String expected = ("<text xml:id='text-1'>"//
        + "<div xml:id='div-1'>"//
        + "<p xml:id='p1'>par 1</p>"//
        + "<p xml:id='p-1'>par 2</p>"//
        + "</div>"//
        + "</text>").replace("'", "\"");
    BaseLayerDefinition def = BaseLayerDefinition//
        .withBaseElements(//
            BaseElementDefinition.withName("text"), //
            BaseElementDefinition.withName("div"), //
            BaseElementDefinition.withName("p") //
    );

    // when
    BaseLayerData baseLayerData = TextUtil.extractBaseLayerData(xml, def);

    // then expect
    assertThat(baseLayerData.validationFailed()).isFalse();
    assertThat(baseLayerData.getBaseLayer()).isEqualTo(expected);
  }

}
