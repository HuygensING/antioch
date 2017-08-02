package nl.knaw.huygens.alexandria.api.model.text.view;

/*
 * #%L
 * alexandria-api
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.Test;

import com.google.common.collect.ImmutableList;

public class TextViewDefinitionParserTest {
  @Test
  public void testParseAnnotationLayers() {
    Map<String, List<String>> annotationLayers = new HashMap();
    List<String> layer1 = ImmutableList.of("a", "b", "c");
    annotationLayers.put("layer1", layer1);
    List<String> layer2 = ImmutableList.of("X", "Y", "Z");
    annotationLayers.put("layer2", layer2);
    List<String> layer3 = ImmutableList.of("1", "2", "3");
    annotationLayers.put("layer3", layer3);
    List<String> annotationLayerDepthOrder = ImmutableList.of("layer2", "layer1", "layer3");
    TextViewDefinition definition = new TextViewDefinition()//
        .setAnnotationLayers(annotationLayers)//
        .setAnnotationLayerDepthOrder(annotationLayerDepthOrder);
    TextViewDefinitionParser parser = new TextViewDefinitionParser(definition);
    assertThat(parser.isValid()).isTrue();
    Optional<TextView> oTextView = parser.getTextView();
    assertThat(oTextView).isPresent();
    TextView textView = oTextView.get();
    assertThat(textView.getOrderedLayerTags()).containsExactly(layer2, layer1, layer3);
  }

  @Test
  public void testParseAnnotationLayersWithErrors() {
    Map<String, List<String>> annotationLayers = new HashMap();
    List<String> layer1 = ImmutableList.of("a", "b", "c");
    annotationLayers.put("layer1", layer1);
    List<String> layer2 = ImmutableList.of("X", "Y", "Z");
    annotationLayers.put("layer2", layer2);
    List<String> annotationLayerDepthOrder = ImmutableList.of("layer4", "layer1");
    TextViewDefinition definition = new TextViewDefinition()//
        .setAnnotationLayers(annotationLayers)//
        .setAnnotationLayerDepthOrder(annotationLayerDepthOrder);
    TextViewDefinitionParser parser = new TextViewDefinitionParser(definition);
    assertThat(parser.isValid()).isFalse();
    assertThat(parser.getErrors()).containsExactly("annotationLayerDepthOrder: layer4 not defined in annotationLayers");
  }

}
