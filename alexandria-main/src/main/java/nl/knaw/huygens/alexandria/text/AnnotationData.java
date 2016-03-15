package nl.knaw.huygens.alexandria.text;

import java.text.MessageFormat;
import java.util.stream.Collectors;

import nl.knaw.huygens.tei.Element;

public class AnnotationData {
  String annotatedBaseText = "";
  XmlAnnotationLevel level = XmlAnnotationLevel.element;
  String type = "";
  String xpath = "";
  Object value;

  public String getAnnotatedBaseText() {
    return annotatedBaseText;
  }

  public AnnotationData setAnnotatedBaseText(String annotatedBaseText) {
    this.annotatedBaseText = annotatedBaseText;
    return this;
  }

  public XmlAnnotationLevel getLevel() {
    return level;
  }

  public AnnotationData setLevel(XmlAnnotationLevel level) {
    this.level = level;
    return this;
  }

  public String getXPath() {
    return xpath;
  }

  public AnnotationData setXPath(String xpath) {
    this.xpath = xpath;
    return this;
  }

  public String getType() {
    return type;
  }

  public AnnotationData setType(String type) {
    this.type = type;
    return this;
  }

  public Object getValue() {
    return value;
  }

  public AnnotationData setValue(Object value) {
    this.value = value;
    return this;
  }

  public String toVerbose() {
    if (getLevel().equals(XmlAnnotationLevel.element)) {
      Element element = (Element) value;
      String prefix = ", with ";
      String attributeAnnotationActions = element.getAttributes().entrySet().stream()//
          .map(kv -> "attribute annotation '" + kv.getKey() + "'='" + kv.getValue() + "'")//
          .collect(Collectors.joining(", and ", prefix, ""));

      if (attributeAnnotationActions.equals(prefix)) {
        attributeAnnotationActions = "";
      }

      return (MessageFormat.format(//
          "adding element annotation on text ''{0}'', on xpath ''{1}'' element={2}{3}", //
          annotatedBaseText, //
          xpath, //
          type, //
          attributeAnnotationActions)//
      );
    }

    return "adding attribute annotation on xpath '" + xpath + "' : " + type + "=" + value;
  }

}
