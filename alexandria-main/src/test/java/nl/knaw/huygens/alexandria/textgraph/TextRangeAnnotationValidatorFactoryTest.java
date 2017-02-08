package nl.knaw.huygens.alexandria.textgraph;

/*
 * #%L
 * alexandria-main
 * =======
 * Copyright (C) 2015 - 2017 Huygens ING (KNAW)
 * =======
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

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
