package nl.knaw.huygens.alexandria.text;

import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import nl.knaw.huygens.Log;
import nl.knaw.huygens.alexandria.api.model.BaseElementDefinition;
import nl.knaw.huygens.alexandria.api.model.BaseLayerDefinition;
import nl.knaw.huygens.tei.Document;
import nl.knaw.huygens.tei.Element;

public class TextUtil {
  static final String XML_ID = "xml:id";
  static final String SUBTEXTPLACEHOLDER = "alexandria_subtextplaceholder";

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

    return extractSubResourceTexts(def, xmlWithIds);
  }

  private static BaseLayerData extractSubResourceTexts(BaseLayerDefinition def, String xml) {
    // extract base layer + nearest layer of subresource xml
    Document documentWithIds = Document.createFromXml(xml, true);
    SubresourceElementVisitor subresourceVisitor = new SubresourceElementVisitor(def.getSubresourceElements());
    documentWithIds.accept(subresourceVisitor);
    String baseText = subresourceVisitor.getBaseText();

    Document baseDocument = Document.createFromXml(baseText, true);
    BaseLayerVisitor blVisitor = new BaseLayerVisitor(def);
    baseDocument.accept(blVisitor);
    Map<String, String> subresourceXPathMap = blVisitor.getSubresourceXPathMap();
    for (Entry<String, String> entry : subresourceVisitor.getSubresourceTexts().entrySet()) {
      String id = entry.getKey();
      String text = entry.getValue();
      // subresourceVisitor.getSubresourceTexts().forEach((id, text) -> {
      String xpath = subresourceXPathMap.get(id);
      BaseLayerData subLayerData = extractSubResourceTexts(def, text);
      Log.info("subLayerData={}", subLayerData);
      Log.info("make subresource with sub=\"xpath:{}\" and text={}", xpath, subLayerData.getBaseLayer());
      // });
    }

    return blVisitor.getBaseLayerData();
  }

  public static String xpath(Element element) {
    String xpath = "";
    if (element.hasAttribute(XML_ID)) {
      xpath = "//" + element.getName() + "[@xml:id='" + element.getAttribute(XML_ID) + "']";
    } else {
      Element parent = element.getParent();
      if (parent == null) {
        xpath = "/" + element.getName();
      } else {
        xpath = xpath(parent) + "/" + element.getName();
      }
    }
    return xpath;
  }

  public static String substringXPath(Element parent, int start, int length) {
    Log.info("offset: (start={},length={})", start, length);
    return "substring(" + xpath(parent) + "," + start + "," + length + ")";
  }

  static final String SUBTEXTID_PREFIX = "alexandria:subtext-";

}
