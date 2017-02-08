package nl.knaw.huygens.alexandria.textgraph;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import nl.knaw.huygens.alexandria.api.model.text.TextRangeAnnotation.Position;
import nl.knaw.huygens.alexandria.test.AlexandriaTest;

public class TextRangeAnnotationValidatorFactoryTest extends AlexandriaTest {

  @Test
    public void testValidateNewPositionWithoutOffsetAndLength() throws Exception {
      Position position = new Position().setXmlId("p-1");
      String xml = singleQuotesToDouble("<text><p xml:id='p-1'>bladiebla</p><p xml:id='p-2'>etc.</p></text>");
      String annotated = TextRangeAnnotationValidatorFactory.getAnnotatedText(position, xml);
      assertThat(annotated).isEqualTo("bladiebla");
    }

  @Test
    public void testValidateNewPositionWithOffsetAndLength() throws Exception {
      Position position = new Position().setXmlId("p-1").setOffset(4).setLength(3);
      String xml = singleQuotesToDouble("<text><p xml:id='p-1'>bladiebla</p><p xml:id='p-2'>etc.</p></text>");
      String annotated = TextRangeAnnotationValidatorFactory.getAnnotatedText(position, xml);
      assertThat(annotated).isEqualTo("die");
    }

}
