package nl.knaw.huygens.alexandria.text;

/*
 * #%L
 * alexandria-main
 * =======
 * Copyright (C) 2015 - 2016 Huygens ING (KNAW)
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

import static java.util.stream.Collectors.joining;

import org.junit.Test;

import com.google.common.collect.ImmutableList;

import nl.knaw.huygens.Log;
import nl.knaw.huygens.alexandria.api.model.BaseElementDefinition;
import nl.knaw.huygens.alexandria.api.model.BaseLayerDefinition;
import nl.knaw.huygens.alexandria.test.AlexandriaTest;

public class TextUtilTest extends AlexandriaTest {
  @Test
  public void testBaseLayerExtraction() {
    // given
    String xml = singleQuotesToDouble("<text>"//
        + "<div xml:id='div-1' lang='nl'>"//
        + "<p xml:id='p1' rend='red'>paragraph with <b><i rend='yes'>text</i></b></p>"//
        + "<p>two</p>"//
        + "</div>"//
        + "<div xml:id='div-2' lang='nl'>"//
        + "<p>three</p>"//
        + "</div>"//
        + "</text>");
    String expected = singleQuotesToDouble("<text xml:id='text-1'>"//
        + "<div xml:id='div-1'>"//
        + "<p xml:id='p1'>paragraph with text</p>"//
        + "<p xml:id='p-1'>two</p>"//
        + "</div>"//
        + "<div xml:id='div-2'>"//
        + "<p xml:id='p-2'>three</p>"//
        + "</div>"//
        + "</text>");
    BaseLayerDefinition def = BaseLayerDefinition//
        .withBaseElements(//
            BaseElementDefinition.withName("text"), //
            BaseElementDefinition.withName("div").withAttributes("xml:id"), //
            BaseElementDefinition.withName("p").withAttributes("xml:id") //
    );

    // when
    BaseLayerData baseLayerData = TextUtil.extractBaseLayerData(xml, def);
    Log.info("AnnotationData =\n{}", baseLayerData.getAnnotationData().stream().map(AnnotationData::toVerbose).collect(joining("\n")));
    String baseLayer = baseLayerData.getBaseLayer();

    // then expect
    softly.assertThat(baseLayerData.validationFailed()).isFalse();
    softly.assertThat(baseLayer).isEqualTo(expected);
    Log.info(expected);
  }

  @Test
  public void testBaseLayerExtractionFailsOnRootElementNotInBaseLayerDefinition() {
    // given
    String xml = singleQuotesToDouble("<text>"//
        + "<div xml:id='div-1' lang='nl'>"//
        + "<p xml:id='p1' rend='red'>paragraph with <b><i rend='yes'>text</i></b></p>"//
        + "</div>"//
        + "</text>");
    BaseLayerDefinition def = BaseLayerDefinition//
        .withBaseElements(//
            BaseElementDefinition.withName("div").withAttributes("xml:id"), //
            BaseElementDefinition.withName("p").withAttributes("xml:id") //
    );

    // when
    BaseLayerData baseLayerData = TextUtil.extractBaseLayerData(xml, def);

    // then expect
    softly.assertThat(baseLayerData.validationFailed()).isTrue();
    softly.assertThat(baseLayerData.getValidationErrors()).contains("Validation error: root element <text> is not in the base layer definition.");
  }

  @Test
  public void testBaseLayerExtractionAddsMissingXmlIdsToBaseElements() {
    // given
    String xml = singleQuotesToDouble("<text>"//
        + "<div xml:id='div-1'>"//
        + "<p xml:id='p1'>par <num>1</num></p>"//
        + "<p>par 2</p>"//
        + "</div>"//
        + "</text>");
    String expected = singleQuotesToDouble("<text xml:id='text-1'>"//
        + "<div xml:id='div-1'>"//
        + "<p xml:id='p1'>par 1</p>"//
        + "<p xml:id='p-1'>par 2</p>"//
        + "</div>"//
        + "</text>");
    BaseLayerDefinition def = BaseLayerDefinition//
        .withBaseElements(//
            BaseElementDefinition.withName("text"), //
            BaseElementDefinition.withName("div"), //
            BaseElementDefinition.withName("p") //
    );

    // when
    BaseLayerData baseLayerData = TextUtil.extractBaseLayerData(xml, def);

    // then expect
    softly.assertThat(baseLayerData.validationFailed()).isFalse();
    softly.assertThat(baseLayerData.getBaseLayer()).isEqualTo(expected);
  }

  @Test
  public void testBaseLayerExtractionWithNestedSubResourceTexts() {
    // given
    String xml = singleQuotesToDouble("<text>"//
        + "<div>"//
        + "<p>Lorem Ipsum Dolor Etc.<note>Damned<note>Is this really necessary?</note> interesting, please continue</note></p>"//
        + "</div>"//
        + "</text>");
    String expected = singleQuotesToDouble("<text xml:id='text-1'>"//
        + "<div xml:id='div-1'>"//
        + "<p xml:id='p-1'>Lorem Ipsum Dolor Etc.</p>"//
        + "</div>"//
        + "</text>");
    BaseLayerDefinition def = BaseLayerDefinition.withBaseElements(//
        BaseElementDefinition.withName("text"), //
        BaseElementDefinition.withName("div").withAttributes("xml:id"), //
        BaseElementDefinition.withName("p").withAttributes("xml:id") //
    ).setSubresourceElements(ImmutableList.of("note"));

    // when
    BaseLayerData baseLayerData = TextUtil.extractBaseLayerData(xml, def);
    Log.info("AnnotationData =\n{}", baseLayerData.getAnnotationData().stream().map(AnnotationData::toVerbose).collect(joining("\n")));
    String baseLayer = baseLayerData.getBaseLayer();

    // then expect
    softly.assertThat(baseLayerData.validationFailed()).isFalse();
    softly.assertThat(baseLayer).isEqualTo(expected);
    Log.info(expected);
    baseLayerData.getAnnotationData().forEach(a -> Log.info(">> {}", a.toVerbose()));
  }

}
