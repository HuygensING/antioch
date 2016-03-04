package nl.knaw.huygens.alexandria.text;

import java.util.List;

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
        + "<p xml:id='p-1'>foo<alexandria:subtextplaceholder xml:id='alexandria:subtext-1'/></p>"//
        + "<p xml:id='p-2'>bar<alexandria:subtextplaceholder xml:id='alexandria:subtext-2'/></p>"//
        + "</text>");
    String expectedSubText1 = singleQuotesToDouble("<note n='1'>probably by <persName>someone</persName></note>");
    String expectedSubText2 = singleQuotesToDouble("<comment n='2'>most likely by <persName>someone else</persName></comment>");
    List<String> subresourceElements = ImmutableList.of("note", "comment");
    SubresourceElementVisitor subresourceVisitor = new SubresourceElementVisitor(subresourceElements);

    // when
    visitXml(xml, subresourceVisitor);

    // expect
    softly.assertThat(subresourceVisitor.getBaseText()).isEqualTo(expectedBase);
    softly.assertThat(subresourceVisitor.getSubresourceTexts()).hasSize(2);
    softly.assertThat(subresourceVisitor.getSubresourceTexts()).containsEntry("alexandria:subtext-1", expectedSubText1);
    softly.assertThat(subresourceVisitor.getSubresourceTexts()).containsEntry("alexandria:subtext-2", expectedSubText2);
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
        + "<p xml:id='p-1'>foo<alexandria:subtextplaceholder xml:id='alexandria:subtext-1'/></p>"//
        + "</text>");
    String expectedSubText = singleQuotesToDouble("<note n='1'>"//
        + "probably by <persName>someone</persName>"//
        + "<note n='2'>most likely by <persName>someone else</persName></note>"//
        + "</note>");
    List<String> subresourceElements = ImmutableList.of("note");
    SubresourceElementVisitor subresourceVisitor = new SubresourceElementVisitor(subresourceElements);

    // when
    visitXml(xml, subresourceVisitor);

    // expect
    softly.assertThat(subresourceVisitor.getBaseText()).isEqualTo(expectedBase);
    softly.assertThat(subresourceVisitor.getSubresourceTexts()).hasSize(1);
    softly.assertThat(subresourceVisitor.getSubresourceTexts()).containsEntry("alexandria:subtext-1", expectedSubText);
  }

}
