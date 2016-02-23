package nl.knaw.huygens.alexandria.textlocator;

public class ByXPathTextLocator implements AlexandriaTextLocator {
  static final String PREFIX = "xpath";
  String xpath;

  public String getXPath() {
    return xpath;
  }

  public void setXPath(String xpath) {
    this.xpath = xpath;
  }

  public AlexandriaTextLocator withXPath(String xpath) {
    setXPath(xpath);
    return this;
  }

  @Override
  public String toString() {
    return PREFIX + ":" + xpath;
  }
}
