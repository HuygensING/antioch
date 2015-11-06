package nl.knaw.huygens.alexandria.text;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.Test;

public class TextUtilsTest {

  @Test
  public void testParser1() {
    TextParseResult result = TextUtils.parse("<text>de <b>kat</b> krabt de <b>krullen</b> van de trap</text>");
    assertThat(result).isNotNull();

    List<TextNode> textNodes = result.getTextNodes();
    List<String> textNodeTexts = textNodes.stream().map(TextNode::getText).collect(toList());
    assertThat(textNodeTexts).containsExactly("de ", "kat", " krabt de ", "krullen", " van de trap");

    TextNode textNode0 = textNodes.get(0);
    assertThat(textNode0.isEmpty()).isFalse();
    assertThat(textNode0.getText()).isEqualTo("de ");

    TextNode textNode1 = textNodes.get(1);
    assertThat(textNode1.getText()).isEqualTo("kat");

    TextNode textNode3 = textNodes.get(3);
    assertThat(textNode3.getText()).isEqualTo("krullen");

    TextNode textNode4 = textNodes.get(4);
    assertThat(textNode4.getText()).isEqualTo(" van de trap");

    List<TextRange> textRanges = result.getTextRanges();
    assertThat(textRanges).hasSize(3);

    // <text>
    TextRange textRange0 = textRanges.get(0);
    assertThat(textRange0.getFirstNode()).isEqualTo(textNode0);
    assertThat(textRange0.getLastNode()).isEqualTo(textNode4);

    // <b>
    TextRange textRange1 = textRanges.get(1);
    assertThat(textRange1.getFirstNode()).isEqualTo(textNode1);
    assertThat(textRange1.getLastNode()).isEqualTo(textNode1);

    // <i>
    TextRange textRange2 = textRanges.get(2);
    assertThat(textRange2.getFirstNode()).isEqualTo(textNode3);
    assertThat(textRange2.getLastNode()).isEqualTo(textNode3);
  }

  @Test
  public void testParsingXmlWithMilestonesLeadsToEmptyTextNodes() {
    TextParseResult result = TextUtils.parse("<text>line one<br/>line two<br/>\nline three</text>");
    assertThat(result).isNotNull();

    List<TextNode> textNodes = result.getTextNodes();
    List<String> textNodeTexts = textNodes.stream().map(TextNode::getText).collect(toList());
    assertThat(textNodeTexts).containsExactly("line one", "", "line two", "", "\nline three");

    TextNode textNode0 = textNodes.get(0);
    assertThat(textNode0.isEmpty()).isFalse();
    assertThat(textNode0.getText()).isEqualTo("line one");

    TextNode textNode1 = textNodes.get(1);
    assertThat(textNode1.isEmpty()).isTrue();
    assertThat(textNode1.getText()).isEqualTo("");

    TextNode textNode3 = textNodes.get(3);
    assertThat(textNode3.isEmpty()).isTrue();
    assertThat(textNode3.getText()).isEqualTo("");

    TextNode textNode4 = textNodes.get(4);
    assertThat(textNode4.getText()).isEqualTo("\nline three");

    List<TextRange> textRanges = result.getTextRanges();
    assertThat(textRanges).hasSize(3);

    // <text>
    TextRange textRange0 = textRanges.get(0);
    assertThat(textRange0.isEmpty()).isFalse();
    assertThat(textRange0.getFirstNode()).isEqualTo(textNode0);
    assertThat(textRange0.getLastNode()).isEqualTo(textNode4);

    // <br/>
    TextRange textRange1 = textRanges.get(1);
    assertThat(textRange1.isEmpty()).isTrue();
    assertThat(textRange1.getFirstNode()).isEqualTo(textNode1);
    assertThat(textRange1.getLastNode()).isEqualTo(textNode1);

    // <br/>
    TextRange textRange2 = textRanges.get(2);
    assertThat(textRange2.isEmpty()).isTrue();
    assertThat(textRange2.getFirstNode()).isEqualTo(textNode3);
    assertThat(textRange2.getLastNode()).isEqualTo(textNode3);
  }

}
