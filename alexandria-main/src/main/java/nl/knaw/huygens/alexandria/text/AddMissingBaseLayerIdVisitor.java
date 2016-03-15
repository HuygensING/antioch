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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import nl.knaw.huygens.tei.Element;
import nl.knaw.huygens.tei.ElementHandler;
import nl.knaw.huygens.tei.Traversal;
import nl.knaw.huygens.tei.XmlContext;
import nl.knaw.huygens.tei.export.ExportVisitor;
import nl.knaw.huygens.tei.handlers.DefaultCommentHandler;
import nl.knaw.huygens.tei.handlers.DefaultProcessingInstructionHandler;
import nl.knaw.huygens.tei.handlers.RenderElementHandler;
import nl.knaw.huygens.tei.handlers.XmlTextHandler;

public class AddMissingBaseLayerIdVisitor extends ExportVisitor {
  private static final String XMLID_MARKER = "-";
  private static List<String> exisitingBaseElementIds;
  private static final Map<String, AtomicLong> counters = new HashMap<>();

  public AddMissingBaseLayerIdVisitor(List<String> exisitingBaseElementIds, List<String> baseElementNames) {
    AddMissingBaseLayerIdVisitor.exisitingBaseElementIds = exisitingBaseElementIds;
    setCommentHandler(new DefaultCommentHandler<>());
    setTextHandler(new XmlTextHandler<>());
    setDefaultElementHandler(new RenderElementHandler());
    setProcessingInstructionHandler(new DefaultProcessingInstructionHandler<>());
    String[] elementNames = baseElementNames.toArray(new String[baseElementNames.size()]);
    addElementHandler(new BaseElementHandler(), elementNames);
    counters.clear();
  }

  static class BaseElementHandler implements ElementHandler<XmlContext> {
    @Override
    public Traversal enterElement(Element element, XmlContext context) {
      if (!element.hasAttribute(TextUtil.XML_ID)) {
        addId(element);
      }
      context.addOpenTag(element);
      return Traversal.NEXT;
    }

    private void addId(Element baseElement) {
      String name = baseElement.getName();
      String id;
      counters.putIfAbsent(name, new AtomicLong(0));
      do {
        id = name + XMLID_MARKER + counters.get(name).incrementAndGet();
      } while (exisitingBaseElementIds.contains(id));
      baseElement.setAttribute(TextUtil.XML_ID, id);
    }

    @Override
    public Traversal leaveElement(Element element, XmlContext context) {
      context.addCloseTag(element);
      return Traversal.NEXT;
    }
  }
}
