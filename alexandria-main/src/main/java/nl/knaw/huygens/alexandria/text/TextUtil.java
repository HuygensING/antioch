package nl.knaw.huygens.alexandria.text;

/*
 * #%L
 * alexandria-main
 * =======
 * Copyright (C) 2015 - 2016 Huygens ING (KNAW)
 * =======
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

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

    return extractSubResourceTexts(def, xmlWithIds, "");
  }

  private static BaseLayerData extractSubResourceTexts(BaseLayerDefinition def, String xml, String parentId) {
    // extract base layer + nearest layer of subresource xml
    Document documentWithIds = Document.createFromXml(xml, true);
    SEVContext context = new SEVContext();
    SubresourceElementVisitor subresourceVisitor = new SubresourceElementVisitor(context, def.getSubresourceElements());
    documentWithIds.accept(subresourceVisitor);
    String baseText = context.getBaseText();

    Document baseDocument = Document.createFromXml(baseText, true);
    BLVContext blContext = new BLVContext();
    BaseLayerVisitor blVisitor = new BaseLayerVisitor(blContext, def, parentId);
    baseDocument.accept(blVisitor);

    BaseLayerData baseLayerData = blVisitor.getBaseLayerData();
    Map<String, String> subresourceXPathMap = blContext.getSubresourceXPathMap();
    for (Entry<String, String> entry : context.getSubresourceTexts().entrySet()) {
      String id = entry.getKey();
      String text = entry.getValue();
      String xpath = subresourceXPathMap.get(id);
      BaseLayerData subLayerData = extractSubResourceTexts(def, text, xpath);
      baseLayerData.getSubLayerData().add(subLayerData);
      Log.info("subLayerData={}", subLayerData);
      Log.info("make subresource with sub=\"xpath:{}\" and text={}", xpath, subLayerData.getBaseLayer());
    }

    return baseLayerData;
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
