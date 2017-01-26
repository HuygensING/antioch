package nl.knaw.huygens.alexandria.api.model.text.view;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.Test;

import com.google.common.collect.ImmutableList;

import nl.knaw.huygens.Log;

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
    List<List<String>> parsed = parser.parseAnnotationLayers(annotationLayers, annotationLayerDepthOrder);
    Log.info("parsed={}", parsed);
    assertThat(parser.isValid()).isTrue();
    assertThat(parsed).hasSize(annotationLayerDepthOrder.size());
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
