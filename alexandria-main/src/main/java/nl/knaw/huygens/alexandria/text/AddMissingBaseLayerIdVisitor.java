package nl.knaw.huygens.alexandria.text;

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
  private static final String XML_ID = "xml:id";
  private static final String XMLID_MARKER = "-";
  private static List<String> exisitingBaseElementIds;
  private static final Map<String, AtomicLong> counters = new HashMap<>();

  public AddMissingBaseLayerIdVisitor(List<String> exisitingBaseElementIds, List<String> baseElementNames) {
    AddMissingBaseLayerIdVisitor.exisitingBaseElementIds = exisitingBaseElementIds;
    setCommentHandler(new DefaultCommentHandler<>());
    setTextHandler(new XmlTextHandler<>());
    setDefaultElementHandler(new RenderElementHandler());
    setProcessingInstructionHandler(new DefaultProcessingInstructionHandler<>());
    String[] elementNames = baseElementNames.toArray(new String[] {});
    addElementHandler(new BaseElementHandler(), elementNames);
    counters.clear();
  }

  static class BaseElementHandler implements ElementHandler<XmlContext> {
    @Override
    public Traversal enterElement(Element element, XmlContext context) {
      if (!element.hasAttribute(XML_ID)) {
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
      baseElement.setAttribute(XML_ID, id);
    }

    @Override
    public Traversal leaveElement(Element element, XmlContext context) {
      context.addCloseTag(element);
      return Traversal.NEXT;
    }
  }
}
