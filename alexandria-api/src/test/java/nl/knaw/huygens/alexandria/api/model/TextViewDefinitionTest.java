package nl.knaw.huygens.alexandria.api.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;

import nl.knaw.huygens.alexandria.api.model.ElementView.AttributeFunction;
import nl.knaw.huygens.alexandria.test.AlexandriaTest;

public class TextViewDefinitionTest extends AlexandriaTest {
  private static ObjectMapper om = new ObjectMapper();

  @BeforeClass
  public static void beforeClass() {
    om.registerModule(new Jdk8Module());
  }

  @Test
  public void testJsonSerializing() throws IOException {
    TextViewDefinition d = new TextViewDefinition();
    d.setDescription("Test definition");
    d.getElementViewDefinitions().put(TextViewDefinition.DEFAULT, new ElementViewDefinition().setElementMode(ElementView.ElementMode.show).setAttributeMode("showAll"));
    d.getElementViewDefinitions().put("note", new ElementViewDefinition().setElementMode(ElementView.ElementMode.hide));
    d.getElementViewDefinitions().put("persName", new ElementViewDefinition().setElementMode(ElementView.ElementMode.show).setWhen("attribute(resp).is(\"#ed\")"));

    TextViewDefinitionParser tvdp = new TextViewDefinitionParser(d);
    assertThat(tvdp.isValid()).isTrue();

    String json = om.writeValueAsString(d);
    String expected = singleQuotesToDouble("{'textView':"//
        + "{"//
        + "'description':'Test definition',"//
        + "'elements':{"//
        + "':default':{'elementMode':'show','attributeMode':'showAll'},"//
        + "'note':{'elementMode':'hide'},"//
        + "'persName':{'elementMode':'show','when':'attribute(resp).is(\\'#ed\\')'}"//
        + "}"//
        + "}}");

    TextViewDefinition d2 = om.readValue(json, TextViewDefinition.class);
    assertThat(json).isEqualTo(expected);
    assertThat(d2).isEqualToComparingFieldByFieldRecursively(d);
  }

  // http://www.w3schools.com/xml/xml_elements.asp :
  // XML elements must follow these naming rules:
  //
  // Element names are case-sensitive
  // Element names must start with a letter or underscore
  // Element names cannot start with the letters xml (or XML, or Xml, etc)
  // Element names can contain letters, digits, hyphens, underscores, and periods
  // Element names cannot contain spaces
  @Test
  public void testValidationOfInvalidElementNamesFails() throws IOException {
    TextViewDefinition d = new TextViewDefinition();
    d.getElementViewDefinitions().put("3lement", new ElementViewDefinition().setElementMode(ElementView.ElementMode.show));
    d.getElementViewDefinitions().put("xmlIsDaBomb", new ElementViewDefinition().setElementMode(ElementView.ElementMode.show));
    d.getElementViewDefinitions().put("element?", new ElementViewDefinition().setElementMode(ElementView.ElementMode.show));
    d.getElementViewDefinitions().put("e le ment", new ElementViewDefinition().setElementMode(ElementView.ElementMode.show));
    TextViewDefinitionParser tvdp = new TextViewDefinitionParser(d);
    assertThat(tvdp.isValid()).isFalse();
    assertThat(tvdp.getErrors()).containsExactly(//
        "\"3lement\" is not a valid element name: element names must start with a letter or underscore.", //
        "\"xmlIsDaBomb\" is not a valid element name: element names cannot start with the letters xml (or XML, or Xml, etc).", //
        "\"element?\" is not a valid element name: element names can only contain letters, digits, hyphens, underscores, and periods.", //
        "\"e le ment\" is not a valid element name: element names cannot contain spaces."//
    );
  }

  @Test
  public void testParsingOfInvalidAttributeModeFails() throws IOException {
    TextViewDefinition d = new TextViewDefinition();
    ElementViewDefinition evd1 = new ElementViewDefinition()//
        .setElementMode(ElementView.ElementMode.show)//
        .setAttributeMode("showNone");
    d.getElementViewDefinitions().put("element", evd1);
    ElementViewDefinition evd2 = new ElementViewDefinition()//
        .setElementMode(ElementView.ElementMode.show)//
        .setAttributeMode("showOnly");
    d.getElementViewDefinitions().put("element2", evd2);
    ElementViewDefinition evd3 = new ElementViewDefinition()//
        .setElementMode(ElementView.ElementMode.show)//
        .setAttributeMode("hideOnly");
    d.getElementViewDefinitions().put("element3", evd3);
    ElementViewDefinition evd4 = new ElementViewDefinition()//
        .setElementMode(ElementView.ElementMode.show)//
        .setAttributeMode("hideAll");
    d.getElementViewDefinitions().put("element4", evd4);
    TextViewDefinitionParser tvdp = new TextViewDefinitionParser(d);
    assertThat(tvdp.isValid()).isFalse();
    assertThat(tvdp.getErrors()).containsExactly(//
        "element: \"showNone\" is not a valid attributeMode. Valid attributeMode values are: \"showAll\", \"showOnly attribute...\", \"hideAll\", \"hideOnly attribute...\".", //
        "element2: \"showOnly\" needs one or more attribute names.", //
        "element3: \"hideOnly\" needs one or more attribute names."//
    );
  }

  @Test
  public void testParsingOfShowOnlyAttributeModeParameters() throws IOException {
    TextViewDefinition tvd = new TextViewDefinition();
    ElementViewDefinition evd = new ElementViewDefinition()//
        .setElementMode(ElementView.ElementMode.show)//
        .setAttributeMode("showOnly xml:id ref");
    tvd.getElementViewDefinitions().put("element", evd);

    TextViewDefinitionParser tvdp = new TextViewDefinitionParser(tvd);
    TextView textView = tvdp.getTextView();
    Map<String, ElementView> elementViewMap = textView.getElementViewMap();

    assertThat(tvdp.isValid()).isTrue();
    assertThat(elementViewMap).containsKey("element");
    ElementView elementView = elementViewMap.get("element");
    assertThat(elementView.getElementMode()).isPresent();
    assertThat(elementView.getElementMode().get()).isEqualTo(ElementView.ElementMode.show);
    assertThat(elementView.getAttributeMode().get()).isEqualTo(ElementView.AttributeMode.showOnly);
    assertThat(elementView.getRelevantAttributes()).containsExactly("xml:id", "ref");
  }

  @Test
  public void testParsingOfHideOnlyAttributeModeParameters() throws IOException {
    TextViewDefinition tvd = new TextViewDefinition();
    ElementViewDefinition evd = new ElementViewDefinition()//
        .setElementMode(ElementView.ElementMode.show)//
        .setAttributeMode("hideOnly note");
    tvd.getElementViewDefinitions().put("element", evd);

    TextViewDefinitionParser tvdp = new TextViewDefinitionParser(tvd);
    TextView textView = tvdp.getTextView();
    Map<String, ElementView> elementViewMap = textView.getElementViewMap();

    assertThat(tvdp.isValid()).isTrue();
    assertThat(elementViewMap).containsKey("element");
    ElementView elementView = elementViewMap.get("element");
    assertThat(elementView.getElementMode()).isPresent();
    assertThat(elementView.getElementMode().get()).isEqualTo(ElementView.ElementMode.show);
    assertThat(elementView.getAttributeMode().get()).isEqualTo(ElementView.AttributeMode.hideOnly);
    assertThat(elementView.getRelevantAttributes()).containsExactly("note");
  }

  @Test
  public void testParsingOfIsWhen() throws IOException {
    TextViewDefinition tvd = new TextViewDefinition();
    ElementViewDefinition evd = new ElementViewDefinition()//
        .setElementMode(ElementView.ElementMode.show)//
        .setWhen("attribute(resp).is('#ed1')");
    tvd.getElementViewDefinitions().put("element", evd);

    TextViewDefinitionParser tvdp = new TextViewDefinitionParser(tvd);
    TextView textView = tvdp.getTextView();
    Map<String, ElementView> elementViewMap = textView.getElementViewMap();

    assertThat(tvdp.isValid()).isTrue();
    assertThat(elementViewMap).containsKey("element");

    ElementView elementView = elementViewMap.get("element");
    assertThat(elementView.getElementMode()).isPresent();
    assertThat(elementView.getElementMode().get()).isEqualTo(ElementView.ElementMode.show);
    assertThat(elementView.getAttributeMode()).isNotPresent();
    assertThat(elementView.getRelevantAttributes()).isEmpty();

    AttributePreCondition preCondition = elementView.getPreCondition().get();
    assertThat(preCondition.getAttribute()).isEqualTo("resp");
    assertThat(preCondition.getFunction()).isEqualTo(AttributeFunction.is);
    assertThat(preCondition.getValues()).containsExactly("#ed1");
  }

  @Test
  public void testParsingOfIsNotWhen() throws IOException {
    TextViewDefinition tvd = new TextViewDefinition();
    ElementViewDefinition evd = new ElementViewDefinition()//
        .setElementMode(ElementView.ElementMode.show)//
        .setWhen("attribute(resp).isNot('#ed2')");
    tvd.getElementViewDefinitions().put("element", evd);

    TextViewDefinitionParser tvdp = new TextViewDefinitionParser(tvd);
    TextView textView = tvdp.getTextView();
    Map<String, ElementView> elementViewMap = textView.getElementViewMap();

    assertThat(tvdp.isValid()).isTrue();
    assertThat(elementViewMap).containsKey("element");

    ElementView elementView = elementViewMap.get("element");
    assertThat(elementView.getElementMode()).isPresent();
    assertThat(elementView.getElementMode().get()).isEqualTo(ElementView.ElementMode.show);
    assertThat(elementView.getAttributeMode()).isNotPresent();
    assertThat(elementView.getRelevantAttributes()).isEmpty();

    AttributePreCondition preCondition = elementView.getPreCondition().get();
    assertThat(preCondition.getAttribute()).isEqualTo("resp");
    assertThat(preCondition.getFunction()).isEqualTo(AttributeFunction.isNot);
    assertThat(preCondition.getValues()).containsExactly("#ed2");
  }

  @Test
  public void testParsingOfFirstOfWhen() throws IOException {
    TextViewDefinition tvd = new TextViewDefinition();
    ElementViewDefinition evd = new ElementViewDefinition()//
        .setElementMode(ElementView.ElementMode.show)//
        .setWhen("attribute(resp).firstOf('#ed1','#ed2','')");
    tvd.getElementViewDefinitions().put("element", evd);

    TextViewDefinitionParser tvdp = new TextViewDefinitionParser(tvd);
    TextView textView = tvdp.getTextView();
    Map<String, ElementView> elementViewMap = textView.getElementViewMap();

    assertThat(tvdp.isValid()).isTrue();
    assertThat(elementViewMap).containsKey("element");

    ElementView elementView = elementViewMap.get("element");
    assertThat(elementView.getElementMode()).isPresent();
    assertThat(elementView.getElementMode().get()).isEqualTo(ElementView.ElementMode.show);
    assertThat(elementView.getAttributeMode()).isNotPresent();
    assertThat(elementView.getRelevantAttributes()).isEmpty();

    AttributePreCondition preCondition = elementView.getPreCondition().get();
    assertThat(preCondition.getAttribute()).isEqualTo("resp");
    assertThat(preCondition.getFunction()).isEqualTo(AttributeFunction.firstOf);
    assertThat(preCondition.getValues()).containsExactly("#ed1", "#ed2", "");
  }

  @Test
  public void testParsingOfInvalidWhen() throws IOException {
    TextViewDefinition tvd = new TextViewDefinition();
    ElementViewDefinition evd = new ElementViewDefinition()//
        .setElementMode(ElementView.ElementMode.show)//
        .setWhen("attribute(resp).myownfunction('#ed1','#ed2','')");
    tvd.getElementViewDefinitions().put("element", evd);

    TextViewDefinitionParser tvdp = new TextViewDefinitionParser(tvd);
    assertThat(tvdp.isValid()).isFalse();
    assertThat(tvdp.getErrors()).containsExactly(
        "element: \"attribute(resp).myownfunction('#ed1','#ed2','')\" is not a valid condition. Valid when values are: \"attribute(a).is('value')\", \"attribute(a).isNot('value')\", \"attribute(a).firstOf('value0','value1',...)\".");
  }
}
