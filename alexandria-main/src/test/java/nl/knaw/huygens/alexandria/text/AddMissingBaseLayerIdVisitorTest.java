package nl.knaw.huygens.alexandria.text;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.Test;

import com.google.common.collect.ImmutableList;

public class AddMissingBaseLayerIdVisitorTest extends AlexandriaVisitorTest {
  @Test
  public void testVisitor() {
    String xml = singleQuotesToDouble("<text>"//
        + "<p xml:id='p-1'>par 1</p>"//
        + "<p>par <num>2</num></p>"//
        + "</text>");
    String expected = singleQuotesToDouble("<text xml:id='text-1'>"//
        + "<p xml:id='p-1'>par 1</p>"//
        + "<p xml:id='p-2'>par <num>2</num></p>"//
        + "</text>");
    List<String> existingBaseElementIds = ImmutableList.of("p-1");
    List<String> baseElementNames = ImmutableList.of("text", "p");
    AddMissingBaseLayerIdVisitor addIdVisitor = new AddMissingBaseLayerIdVisitor(existingBaseElementIds, baseElementNames);

    visitXml(xml, addIdVisitor);

    String xmlWithIds = addIdVisitor.getContext().getResult();
    assertThat(xmlWithIds).isEqualTo(expected);
  }

}
