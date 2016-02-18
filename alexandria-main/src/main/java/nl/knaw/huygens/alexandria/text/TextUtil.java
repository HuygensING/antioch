package nl.knaw.huygens.alexandria.text;

import nl.knaw.huygens.alexandria.model.BaseLayerDefinition;
import nl.knaw.huygens.tei.Document;

public class TextUtil {
  public static BaseLayerData extractBaseLayerData(String xml, BaseLayerDefinition def) {
    Document document = Document.createFromXml(xml, true);
    BaseLayerVisitor visitor = new BaseLayerVisitor(def);
    document.accept(visitor);
    // visitor.getAnnotationActions().forEach(s -> {
    // Log.info("annotation:{}", s);
    // });

    return visitor.getBaseLayerData();
  }
}
