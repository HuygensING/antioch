package nl.knaw.huygens.alexandria.text;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

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
  private static Map<String, String> subresourceTexts = new HashMap<>();
  public static boolean inSubresourceText = false;
  static AtomicInteger subtextCounter = new AtomicInteger(1);

  public SubresourceElementVisitor(List<String> subresourceElements) {
    setCommentHandler(new DefaultCommentHandler<>());
    setTextHandler(new XmlTextHandler<>());
    setDefaultElementHandler(new RenderElementHandler());
    setProcessingInstructionHandler(new DefaultProcessingInstructionHandler<>());
    String[] elementNames = subresourceElements.toArray(new String[subresourceElements.size()]);
    addElementHandler(new SubResourceElementHandler(), elementNames);
    subresourceTexts.clear();
    subtextCounter.set(1);
  }

  public String getBaseText() {
    return getContext().getResult();
  }

  public Map<String, String> getSubresourceTexts() {
    return subresourceTexts;
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
        String subtextId = TextUtil.SUBTEXTID_PREFIX + subtextCounter.getAndIncrement();
        subresourceTexts.put(subtextId, context.closeLayer());
        Element placeHolder = new Element(TextUtil.SUBTEXTPLACEHOLDER).withAttribute(TextUtil.XML_ID, subtextId);
        context.addEmptyElementTag(placeHolder);
        subresourceElement = null;
        inSubresourceText = false;
      }
      return Traversal.NEXT;
    }
  }

}
