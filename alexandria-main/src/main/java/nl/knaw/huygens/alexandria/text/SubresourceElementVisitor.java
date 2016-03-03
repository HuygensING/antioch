package nl.knaw.huygens.alexandria.text;

import java.util.ArrayList;
import java.util.List;

import nl.knaw.huygens.tei.Element;
import nl.knaw.huygens.tei.ElementHandler;
import nl.knaw.huygens.tei.Traversal;
import nl.knaw.huygens.tei.XmlContext;
import nl.knaw.huygens.tei.export.ExportVisitor;
import nl.knaw.huygens.tei.handlers.DefaultCommentHandler;
import nl.knaw.huygens.tei.handlers.DefaultProcessingInstructionHandler;
import nl.knaw.huygens.tei.handlers.RenderElementHandler;
import nl.knaw.huygens.tei.handlers.XmlTextHandler;

public class SubresourceElementVisitor extends ExportVisitor {
  private static List<String> subresourceTexts = new ArrayList<>();
  public static boolean inSubresourceText = false;

  public SubresourceElementVisitor(List<String> subresourceElements) {
    setCommentHandler(new DefaultCommentHandler<>());
    setTextHandler(new XmlTextHandler<>());
    setDefaultElementHandler(new RenderElementHandler());
    setProcessingInstructionHandler(new DefaultProcessingInstructionHandler<>());
    String[] elementNames = subresourceElements.toArray(new String[subresourceElements.size()]);
    addElementHandler(new SubResourceElementHandler(), elementNames);
    subresourceTexts.clear();
  }

  public String getBaseText() {
    return getContext().getResult();
  }

  static class SubResourceElementHandler implements ElementHandler<XmlContext> {
    private Element subresourceElement;

    @Override
    public Traversal enterElement(Element element, XmlContext context) {
      if (!inSubresourceText) {
        context.openLayer();
        inSubresourceText = true;
        subresourceElement = element;
      }
      context.addOpenTag(element);
      return Traversal.NEXT;
    }

    @Override
    public Traversal leaveElement(Element element, XmlContext context) {
      context.addCloseTag(element);
      if (element.equals(subresourceElement)) {
        subresourceTexts.add(context.closeLayer());
        subresourceElement = null;
        inSubresourceText = false;
      }
      return Traversal.NEXT;
    }
  }

  public List<String> getSubresourceTexts() {
    return subresourceTexts;
  }

}
