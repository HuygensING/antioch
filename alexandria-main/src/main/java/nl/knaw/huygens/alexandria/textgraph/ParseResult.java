package nl.knaw.huygens.alexandria.textgraph;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class ParseResult {

  private List<String> textSegments = new ArrayList<>();
  private Set<XmlAnnotation> xmlAnnotations = new TreeSet<>();

  public List<String> getTextSegments() {
    return textSegments;
  }

  public Set<XmlAnnotation> getXmlAnnotations() {
    return xmlAnnotations;
  }

}
