package nl.knaw.huygens.alexandria.text;

/*
 * #%L
 * alexandria-main
 * =======
 * Copyright (C) 2015 - 2017 Huygens ING (KNAW)
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

import java.util.ArrayList;
import java.util.List;

import nl.knaw.huygens.tei.Comment;
import nl.knaw.huygens.tei.CommentHandler;
import nl.knaw.huygens.tei.Element;
import nl.knaw.huygens.tei.ElementHandler;
import nl.knaw.huygens.tei.ProcessingInstruction;
import nl.knaw.huygens.tei.ProcessingInstructionHandler;
import nl.knaw.huygens.tei.Traversal;
import nl.knaw.huygens.tei.XmlContext;
import nl.knaw.huygens.tei.export.ExportVisitor;
import nl.knaw.huygens.tei.handlers.XmlTextHandler;

public class XmlIdVisitor extends ExportVisitor implements CommentHandler<XmlContext>, ElementHandler<XmlContext>, ProcessingInstructionHandler<XmlContext> {
  private static List<String> xmlIds = new ArrayList<>();

  public XmlIdVisitor() {
    super();
    setCommentHandler(this);
    setTextHandler(new XmlTextHandler<>());
    setDefaultElementHandler(this);
    setProcessingInstructionHandler(this);
    xmlIds.clear();
  }

  @Override
  public Traversal enterElement(Element element, XmlContext context) {
    if (element.hasAttribute(TextUtil.XML_ID)) {
      xmlIds.add(element.getAttribute(TextUtil.XML_ID));
    }
    return Traversal.NEXT;
  }

  @Override
  public Traversal leaveElement(Element element, XmlContext context) {
    return Traversal.NEXT;
  }

  @Override
  public Traversal visitComment(Comment comment, XmlContext context) {
    return Traversal.NEXT;
  }

  @Override
  public Traversal visitProcessingInstruction(ProcessingInstruction processingInstruction, XmlContext context) {
    return Traversal.NEXT;
  }

  public List<String> getXmlIds() {
    return xmlIds;
  }

}
