package nl.knaw.huygens.alexandria.text;

import nl.knaw.huygens.alexandria.model.BaseLayerDefinition;
import nl.knaw.huygens.tei.Document;

public class TextUtil {
  public static BaseLayerData extractBaseLayerData(String xml, BaseLayerDefinition def) {
    Document document = Document.createFromXml(xml, true);
    BaseLayerIdVisitor idVisitor = new BaseLayerIdVisitor(def);
    document.accept(idVisitor);
    BaseLayerVisitor visitor = new BaseLayerVisitor(def, idVisitor.getBaseElementIds());
    document.accept(visitor);
    // visitor.getAnnotationActions().forEach(s -> {
    // Log.info("annotation:{}", s);
    // });

    return visitor.getBaseLayerData();
  }
}
