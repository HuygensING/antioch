package nl.knaw.huygens.alexandria.text;

public class AnnotationData {
  String annotatedBaseText = "";
  XmlAnnotationLevel level = XmlAnnotationLevel.element;
  String type = "";
  Object value;

  public String getAnnotatedBaseText() {
    return annotatedBaseText;
  }

  public void setAnnotatedBaseText(String annotatedBaseText) {
    this.annotatedBaseText = annotatedBaseText;
  }

  public XmlAnnotationLevel getLevel() {
    return level;
  }

  public void setLevel(XmlAnnotationLevel level) {
    this.level = level;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public Object getValue() {
    return value;
  }

  public void setValue(Object value) {
    this.value = value;
  }

}
