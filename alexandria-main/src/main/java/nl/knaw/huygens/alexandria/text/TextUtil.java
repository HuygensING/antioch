package nl.knaw.huygens.alexandria.text;

import static java.util.stream.Collectors.toList;

import java.util.List;

import nl.knaw.huygens.alexandria.api.model.BaseElementDefinition;
import nl.knaw.huygens.alexandria.api.model.BaseLayerDefinition;
import nl.knaw.huygens.tei.Document;

public class TextUtil {
  public static BaseLayerData extractBaseLayerData(String xml, BaseLayerDefinition def) {
    Document document = Document.createFromXml(xml, true);
    List<String> baseElementNames = def.getBaseElementDefinitions()//
        .stream()//
        .map(BaseElementDefinition::getName)//
        .collect(toList());

    // determine existing base element ids
    BaseLayerIdVisitor idVisitor = new BaseLayerIdVisitor(baseElementNames);
    document.accept(idVisitor);
    List<String> existingBaseElementIds = idVisitor.getBaseElementIds();

    // set xml:id on base elements where missing
    AddMissingBaseLayerIdVisitor addIdVisitor = new AddMissingBaseLayerIdVisitor(existingBaseElementIds, baseElementNames);
    document.accept(addIdVisitor);
    String xmlWithIds = addIdVisitor.getContext().getResult();

    // extract base layer + nearest layer of subresource xml
    Document documentWithIds = Document.createFromXml(xmlWithIds, true);
    SubresourceElementVisitor subresourceVisitor = new SubresourceElementVisitor(def.getSubresourceElements());
    documentWithIds.accept(subresourceVisitor);
    String baseText = subresourceVisitor.getBaseText();

    Document baseDocument = Document.createFromXml(baseText, true);
    BaseLayerVisitor blVisitor = new BaseLayerVisitor(def);
    baseDocument.accept(blVisitor);
    // visitor.getAnnotationActions().forEach(s -> {
    // Log.info("annotation:{}", s);
    // });

    return blVisitor.getBaseLayerData();
  }
}
