package nl.knaw.huygens.alexandria.text;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.Test;

import com.google.common.collect.ImmutableList;

import nl.knaw.huygens.alexandria.test.AlexandriaTest;
import nl.knaw.huygens.tei.Document;

public class AddMissingBaseLayerIdVisitorTest extends AlexandriaTest {
  @Test
  public void testVisitor() {
    String xml = fixQuotes("<text>"//
        + "<p xml:id='p-1'>par 1</p>"//
        + "<p>par <num>2</num></p>"//
        + "</text>");
    String expected = fixQuotes("<text xml:id='text-1'>"//
        + "<p xml:id='p-1'>par 1</p>"//
        + "<p xml:id='p-2'>par <num>2</num></p>"//
        + "</text>");
    Document document = Document.createFromXml(xml, true);
    List<String> existingBaseElementIds = ImmutableList.of("p-1");
    List<String> baseElementNames = ImmutableList.of("text", "p");

    AddMissingBaseLayerIdVisitor addIdVisitor = new AddMissingBaseLayerIdVisitor(existingBaseElementIds, baseElementNames);
    document.accept(addIdVisitor);
    String xmlWithIds = addIdVisitor.getContext().getResult();
    assertThat(xmlWithIds).isEqualTo(expected);
  }

}
