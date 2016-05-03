package nl.knaw.huygens.alexandria.api.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.List;

import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import nl.knaw.huygens.alexandria.api.model.ElementViewDefinition.ElementMode;
import nl.knaw.huygens.alexandria.test.AlexandriaTest;

public class TextViewDefinitionTest extends AlexandriaTest {
  private ObjectMapper om = new ObjectMapper();

  @Test
  public void testJsonSerializing() throws IOException {
    TextViewDefinition d = new TextViewDefinition();
    d.setDescription("Test definition");
    d.getElementViewDefinitions().put(TextViewDefinition.DEFAULT, new ElementViewDefinition().setElementMode(ElementMode.show).setAttributeMode("showAll"));
    d.getElementViewDefinitions().put("note", new ElementViewDefinition().setElementMode(ElementMode.hide));
    d.getElementViewDefinitions().put("persName", new ElementViewDefinition().setElementMode(ElementMode.show).setWhen("attribute(resp).is(\"#ed\")"));
    assertThat(d.isValid()).isTrue();
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
    assertThat(json).isEqualTo(expected);
    TextViewDefinition d2 = om.readValue(json, TextViewDefinition.class);
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
  // @Test
  public void testValidationOfInvalidElementNamesFails() throws IOException {
    TextViewDefinition d = new TextViewDefinition();
    d.getElementViewDefinitions().put("3lement", new ElementViewDefinition().setElementMode(ElementMode.show));
    d.getElementViewDefinitions().put("xmlIsDaBomb", new ElementViewDefinition().setElementMode(ElementMode.show));
    d.getElementViewDefinitions().put("element?", new ElementViewDefinition().setElementMode(ElementMode.show));
    d.getElementViewDefinitions().put("e le ment", new ElementViewDefinition().setElementMode(ElementMode.show));
    assertThat(d.isValid()).isFalse();
    List<String> errors = d.validate();
    assertThat(errors).containsExactly(//
        "\"3lement\" is not a valid element name: element names must start with a letter or underscore.",//
        "\"xmlIsDaBomb\" is not a valid element name: element names cannot start with the letters xml (or XML, or Xml, etc).",//
        "\"element?\" is not a valid element name: element names can only contain letters, digits, hyphens, underscores, and periods.",//
        "\"e le ment\" is not a valid element name: element names cannot contain spaces."//
        );
  }

}
