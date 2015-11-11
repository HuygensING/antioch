package nl.knaw.huygens.alexandria.text;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.junit.Test;

import nl.knaw.huygens.Log;

public class TextUtilsTest {

  @Test
  public void testParser1() {
    TextParseResult result = TextUtils.parse("<text>de <b>kat</b> krabt de <b>krullen</b> van de trap</text>");
    assertThat(result.isOK()).isTrue();

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

    Map<Tag, TextRange> tag2TextRangeMap = result.getTag2TextRangeMap();
    assertThat(tag2TextRangeMap).isNotEmpty();
    Log.info("map={}", tag2TextRangeMap);
  }

  @Test
  public void testParsingXmlWithMilestonesLeadsToEmptyTextNodes() {
    TextParseResult result = TextUtils.parse("<text>line one<br/>line two<br/>\nline three</text>");
    assertThat(result.isOK()).isTrue();

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

    Map<Tag, TextRange> tag2TextRangeMap = result.getTag2TextRangeMap();
    Log.info("map={}", tag2TextRangeMap);
    assertThat(tag2TextRangeMap).isNotEmpty();
    assertThat(tag2TextRangeMap).hasSize(3);
    assertThat(tag2TextRangeMap).containsValues(textRange0, textRange1, textRange2);
    List<String> tagNames = tag2TextRangeMap.keySet().stream().map(Tag::getName).sorted().collect(toList());
    assertThat(tagNames).containsExactly("br", "br", "text");
  }

  @Test
  public void testParser() {
    TextParseResult result = TextUtils.parse("<text>\n"//
        + "<div type=\"opener\" resp=\"WR\">\n"//
        + "<p>Illustrissime Domine legate,</p>\n"//
        + "</div>\n"//
        + "<p>Commoratus est mecum aliquo tempore filius vester <persName key=\"groot.cornelius.1613-1661\" resp=\"#ed\">Cornelius</persName>"//
        + " et quantumvis nil mihi magis fuerit semper in votis quam ipsum loco aliquo convenienti accommodandi, tamen variis de causis hoc meum"//
        + " destinatum inpeditum est.</p>\n"//
        + "<p>Res nostrae per Germaniam non parum immutatae et contractae sunt. Et hic in Suecia nullum licet studiose quaesitum locum, qui huic"//
        + " eius aetati aptus esset, invenire potui.</p>\n"//
        + "<p>Ut eapropter non improbaverim Illustritatis Vestrae consilium ipsum ad se revocandi, cum et ei illis in partibus uberior se"//
        + " materies offerre possit ingenii juvenilis excolendi et nullibi commodius quam a vobis regi et informari queat.</p>\n"//
        + "<p>Bona igitur gratia ipsum dimisi, non tamen sine aliquo doloris sensu, quod in praesens et meae voluntati et affectui in"//
        + " Illustritatem Vestram omnesque illi annexos flagrantissimo satisfacere nequiverim; velim sibi persuadeat nil me unquam omissurum,"//
        + " quod facere queat ad honoranda ejusdem merita et domus suae stabilimentum ejusque contestandi animum, quod oportunitas nunc defuerit,"//
        + " distulisse, non omisisse.</p>\n"//
        + "<p>De caetero optime valeat et me sibi addictissimum putet.</p>\n"//
        + "<div type=\"closer\" resp=\"WR\">\n"//
        + "<p>Dabantur <placeName key=\"se:stockholm.swe\" resp=\"auto\">Stocholmiae</placeName>, die III/XIII Julij Anni MDCXXXVII.</p>\n"//
        + "<p>Illustritatis Vestrae amicissimus\n"//
        + "<lb/><persName key=\"oxenstierna.axel.1583-1654\" resp=\"auto\">Axelius Oxenstierna</persName> mp.</p>\n"//
        + "</div>\n"//
        + "</text>"//
    );
    assertThat(result.isOK()).isTrue();
    assertThat(result.getTextNodes().get(2).getText()).isEqualTo("Illustrissime Domine legate,");
    Log.info("gremlin:\n{}", GremlinGenerator.from(result));
  }

  @Test
  public void testIncorrectXMLSetsOKtoFalse() {
    TextParseResult result = TextUtils.parse("<text>blabla");
    assertThat(result.isOK()).isFalse();
    assertThat(result.getParseError()).isEqualTo("org.xml.sax.SAXParseException; lineNumber: 1; columnNumber: 13; XML document structures must start and end within the same entity.");
  }

  @Test
  public void testCommentsInXMLAreIgnored() {
    TextParseResult result = TextUtils.parse("<text>blabla<!-- ignore this! --></text>");
    assertThat(result.isOK()).isTrue();
    assertThat(result.getTextNodes()).hasSize(1);
    assertThat(result.getTextNodes().get(0).getText()).isEqualTo("blabla");
    Log.info("{}", result.getTextNodes());

  }

}
