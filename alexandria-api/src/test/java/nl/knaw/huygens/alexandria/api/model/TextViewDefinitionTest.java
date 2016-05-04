package nl.knaw.huygens.alexandria.api.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;

import nl.knaw.huygens.alexandria.api.model.ElementViewDefinition.ElementMode;
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
    d.getElementViewDefinitions().put(TextViewDefinition.DEFAULT, new ElementViewDefinition().setElementMode(ElementMode.show).setAttributeMode("showAll"));
    d.getElementViewDefinitions().put("note", new ElementViewDefinition().setElementMode(ElementMode.hide));
    d.getElementViewDefinitions().put("persName", new ElementViewDefinition().setElementMode(ElementMode.show).setWhen("attribute(resp).is(\"#ed\")"));

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
    d.getElementViewDefinitions().put("3lement", new ElementViewDefinition().setElementMode(ElementMode.show));
    d.getElementViewDefinitions().put("xmlIsDaBomb", new ElementViewDefinition().setElementMode(ElementMode.show));
    d.getElementViewDefinitions().put("element?", new ElementViewDefinition().setElementMode(ElementMode.show));
    d.getElementViewDefinitions().put("e le ment", new ElementViewDefinition().setElementMode(ElementMode.show));
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
  public void testValidationOfInvalidAttributeModeFails() throws IOException {
    TextViewDefinition d = new TextViewDefinition();
    ElementViewDefinition evd1 = new ElementViewDefinition()//
        .setElementMode(ElementMode.show)//
        .setAttributeMode("showNone");
    d.getElementViewDefinitions().put("element", evd1);
    ElementViewDefinition evd2 = new ElementViewDefinition()//
        .setElementMode(ElementMode.show)//
        .setAttributeMode("showOnly()");
    d.getElementViewDefinitions().put("element2", evd2);
    ElementViewDefinition evd3 = new ElementViewDefinition()//
        .setElementMode(ElementMode.show)//
        .setAttributeMode("hideOnly()");
    d.getElementViewDefinitions().put("element3", evd3);
    ElementViewDefinition evd4 = new ElementViewDefinition()//
        .setElementMode(ElementMode.show)//
        .setAttributeMode("hideAll");
    d.getElementViewDefinitions().put("element4", evd4);
    TextViewDefinitionParser tvdp = new TextViewDefinitionParser(d);
    assertThat(tvdp.isValid()).isFalse();
    assertThat(tvdp.getErrors()).containsExactly(//
        "element: \"showNone\" is not a valid attributeMode. Valid attributeMode values are: \"showAll\", \"showOnly(attribute,...)\", \"hideAll\", \"hideOnly(attribute,...)\".", //
        "element2: \"showOnly()\" needs one or more attribute names.", //
        "element3: \"hideOnly()\" needs one or more attribute names."//
    );
  }
}
