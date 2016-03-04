package nl.knaw.huygens.alexandria.text;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.Test;

import com.google.common.collect.ImmutableList;

public class BaseLayerIdVisitorTest extends AlexandriaVisitorTest {
  @Test
  public void testVisitor1() {
    String xml = singleQuotesToDouble("<text>"//
        + "<p xml:id='p-1'>par 1</p>"//
        + "<p>par <num xml:id='num2'>2</num></p>"//
        + "</text>");
    List<String> baseElementNames = ImmutableList.of("text", "p");

    BaseLayerIdVisitor idVisitor = new BaseLayerIdVisitor(baseElementNames);

    visitXml(xml, idVisitor);

    List<String> existingIds = idVisitor.getBaseElementIds();
    assertThat(existingIds).containsExactly("p-1");
  }

  @Test
  public void testVisitor2() {
    String xml = singleQuotesToDouble("<text>"//
        + "<p xml:id='p1'>par 1</p>"//
        + "<p>par <num xml:id='num2'>2</num></p>"//
        + "</text>");
    List<String> baseElementNames = ImmutableList.of("text", "p");

    BaseLayerIdVisitor idVisitor = new BaseLayerIdVisitor(baseElementNames);

    visitXml(xml, idVisitor);

    List<String> existingIds = idVisitor.getBaseElementIds();
    assertThat(existingIds).containsExactly("p1");
  }

}
