package nl.knaw.huygens.alexandria.text;

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
    String expectedSubText1 = singleQuotesToDouble("<text n='1' subtext_type='note'>probably by <persName>someone</persName></text>");
    String expectedSubText2 = singleQuotesToDouble("<text n='2' subtext_type='comment'>most likely by <persName>someone else</persName></text>");
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
    String expectedSubText = singleQuotesToDouble("<text n='1' subtext_type='note'>"//
        + "probably by <persName>someone</persName>"//
        + "<note n='2'>most likely by <persName>someone else</persName></note>"//
        + "</text>");
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
