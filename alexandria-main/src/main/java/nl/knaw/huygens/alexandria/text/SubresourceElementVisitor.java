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

import java.util.List;

import nl.knaw.huygens.tei.DelegatingVisitor;
import nl.knaw.huygens.tei.Element;
import nl.knaw.huygens.tei.ElementHandler;
import nl.knaw.huygens.tei.Traversal;
import nl.knaw.huygens.tei.handlers.DefaultCommentHandler;
import nl.knaw.huygens.tei.handlers.DefaultProcessingInstructionHandler;
import nl.knaw.huygens.tei.handlers.XmlTextHandler;

public class SubresourceElementVisitor extends DelegatingVisitor<SEVContext> {

  public SubresourceElementVisitor(SEVContext context, List<String> subresourceElements) {
    super(context);
    setCommentHandler(new DefaultCommentHandler<>());
    setTextHandler(new XmlTextHandler<>());
    setDefaultElementHandler(new RootRememberingElementHandler());
    setProcessingInstructionHandler(new DefaultProcessingInstructionHandler<>());
    String[] elementNames = subresourceElements.toArray(new String[subresourceElements.size()]);
    addElementHandler(new SubResourceElementHandler(), elementNames);
  }

  static class RootRememberingElementHandler implements ElementHandler<SEVContext> {
    @Override
    public Traversal enterElement(Element element, SEVContext context) {
      if (element.getParent() == null) {
        context.setRootElementName(element.getName());
      }
      if (element.hasChildren()) {
        context.addOpenTag(element);
      } else {
        context.addEmptyElementTag(element);
      }
      return Traversal.NEXT;
    }

    @Override
    public Traversal leaveElement(Element element, SEVContext context) {
      if (element.hasChildren()) {
        context.addCloseTag(element);
      }
      return Traversal.NEXT;
    }
  }

  static class SubResourceElementHandler implements ElementHandler<SEVContext> {
    private Element subresourceElement;

    @Override
    public Traversal enterElement(Element element, SEVContext context) {
      if (!context.inSubresourceText()) {
        context.openLayer();
        context.setInSubresourceText(true);
        subresourceElement = element;
        Element subtextroot = Element.copyOf(element);
        subtextroot.setName(context.getRootElementName());
        subtextroot.setAttribute("subtext_type", element.getName());
        context.addOpenTag(subtextroot);

      } else {
        context.addOpenTag(element);
      }
      return Traversal.NEXT;
    }

    @Override
    public Traversal leaveElement(Element element, SEVContext context) {
      if (element.equals(subresourceElement)) {
        context.addCloseTag(context.getRootElementName());
        String subtextId = TextUtil.SUBTEXTID_PREFIX + context.getSubtextCounter().getAndIncrement();
        context.getSubresourceTexts().put(subtextId, context.closeLayer());
        Element placeHolder = new Element(TextUtil.SUBTEXTPLACEHOLDER).withAttribute(TextUtil.XML_ID, subtextId);
        context.addEmptyElementTag(placeHolder);
        subresourceElement = null;
        context.setInSubresourceText(false);
      } else {
        context.addCloseTag(element);
      }
      return Traversal.NEXT;
    }
  }

}
