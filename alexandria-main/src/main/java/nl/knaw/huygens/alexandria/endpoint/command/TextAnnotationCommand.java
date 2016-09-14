package nl.knaw.huygens.alexandria.endpoint.command;

import static nl.knaw.huygens.alexandria.text.TextUtil.XML_ID;

import nl.knaw.huygens.alexandria.textgraph.TextAnnotation;

public abstract class TextAnnotationCommand extends ResourcesCommand {

  String getXmlId(TextAnnotation textAnnotation) {
    return textAnnotation.getAttributes().get(XML_ID);
  }

  boolean hasXmlId(TextAnnotation textAnnotation) {
    return textAnnotation.getAttributes().containsKey(XML_ID);
  }

}
