package nl.knaw.huygens.alexandria.textgraph;

import java.util.List;
import java.util.Set;

import org.junit.Test;

import com.google.common.base.Joiner;

import nl.knaw.huygens.Log;
import nl.knaw.huygens.alexandria.api.model.ElementDefinition;
import nl.knaw.huygens.alexandria.api.model.TextView;
import nl.knaw.huygens.alexandria.test.AlexandriaTest;

public class TextGraphUtilTest extends AlexandriaTest {
  @Test
  public void testParse() {
    // given
    String xml = singleQuotesToDouble("<text>"//
        + "<div xml:id='div-1' lang='nl'>"//
        + "<p xml:id='p1' rend='red'>paragraph with <b><i rend='yes'>text</i></b></p>"//
        + "<p>two</p>"//
        + "</div><lb/>"//
        + "<div xml:id='div-2' lang='nl'>"//
        + "<p>three</p>"//
        + "</div>"//
        + "</text>");
    String expectedBaseLayer = singleQuotesToDouble("<text>"//
        + "<div xml:id='div-1' lang='nl'>"//
        + "<p xml:id='p1' rend='red'>paragraph with text</p>"//
        + "<p>two</p>"//
        + "</div><lb/>"//
        + "<div xml:id='div-2' lang='nl'>"//
        + "<p>three</p>"//
        + "</div>"//
        + "</text>");

    // when
    ParseResult result = TextGraphUtil.parse(xml);

    // then
    List<String> textSegments = result.getTextSegments();
    softly.assertThat(textSegments).containsExactly("paragraph with ", "text", "two", "", "three");

    Set<XmlAnnotation> xmlAnnotations = result.getXmlAnnotations();
    softly.assertThat(xmlAnnotations).hasSize(9);
    Log.info("annotations = \n\t{}", Joiner.on("\n\t").join(xmlAnnotations));

    TextView baselayerDefinition = TextView.withIncludedElements(//
        ElementDefinition.withName("text"), //
        ElementDefinition.withName("div"), //
        ElementDefinition.withName("lb"), //
        ElementDefinition.withName("p")//
    );
    String baseLayer = TextGraphUtil.renderBaseLayer(textSegments, xmlAnnotations, baselayerDefinition);
    softly.assertThat(baseLayer).isEqualTo(expectedBaseLayer);

  }
}
