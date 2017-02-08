package nl.knaw.huygens.alexandria.textgraph;

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

import java.util.Stack;

import nl.knaw.huygens.tei.Comment;
import nl.knaw.huygens.tei.CommentHandler;
import nl.knaw.huygens.tei.DelegatingVisitor;
import nl.knaw.huygens.tei.Element;
import nl.knaw.huygens.tei.ElementHandler;
import nl.knaw.huygens.tei.ProcessingInstruction;
import nl.knaw.huygens.tei.ProcessingInstructionHandler;
import nl.knaw.huygens.tei.Text;
import nl.knaw.huygens.tei.Traversal;
import nl.knaw.huygens.tei.XmlContext;
import nl.knaw.huygens.tei.handlers.XmlTextHandler;

public class XmlVisitor extends DelegatingVisitor<XmlContext> implements CommentHandler<XmlContext>, ElementHandler<XmlContext>, ProcessingInstructionHandler<XmlContext> {

  private static ParseResult result;
  private Stack<Integer> startIndexStack = new Stack<>();
  private Stack<Element> elementStack = new Stack<>();
  private static boolean lastNodeWasText = false;

  public XmlVisitor(ParseResult result) {
    super(new XmlContext());
    XmlVisitor.result = result;
    setDefaultElementHandler(this);
    setCommentHandler(this);
    setProcessingInstructionHandler(this);
    setTextHandler(new TextSegmentHandler());
  }

  @Override
  public Traversal enterElement(Element element, XmlContext context) {
    elementStack.add(element);
    lastNodeWasText = false;
    startIndexStack.push(result.getTextSegments().size());
    if (element.hasNoChildren()) {
      result.getTextSegments().add("");
    }
    return Traversal.NEXT;
  }

  @Override
  public Traversal leaveElement(Element element, XmlContext context) {
    elementStack.pop();
    lastNodeWasText = false;
    XmlAnnotation xmlAnnotation = new XmlAnnotation(element.getName(), element.getAttributes(), elementStack.size())//
        .setMilestone(element.hasNoChildren())//
        .setFirstSegmentIndex(startIndexStack.pop())//
        .setLastSegmentIndex(result.getTextSegments().size() - 1)//
        ;
    result.getXmlAnnotations().add(xmlAnnotation);
    return Traversal.NEXT;
  }

  @Override
  public Traversal visitProcessingInstruction(ProcessingInstruction processingInstruction, XmlContext context) {
    return Traversal.NEXT;
  }

  @Override
  public Traversal visitComment(Comment comment, XmlContext context) {
    return Traversal.NEXT;
  }

  public static class TextSegmentHandler extends XmlTextHandler<XmlContext> {
    @Override
    public Traversal visitText(Text text, XmlContext context) {
      String filteredText = filterText(text.getText());
      if (lastNodeWasText) {
        int lastIndex = result.getTextSegments().size() - 1;
        String segment = result.getTextSegments().get(lastIndex);
        result.getTextSegments().set(lastIndex, segment + filteredText);
      } else {
        result.getTextSegments().add(filteredText);
      }
      lastNodeWasText = true;
      return Traversal.NEXT;
    }
  }

}
