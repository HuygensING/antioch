package nl.knaw.huygens.alexandria.text;

import java.util.List;

import nl.knaw.huygens.tei.export.ExportVisitor;
import nl.knaw.huygens.tei.handlers.DefaultCommentHandler;
import nl.knaw.huygens.tei.handlers.DefaultProcessingInstructionHandler;
import nl.knaw.huygens.tei.handlers.RenderElementHandler;
import nl.knaw.huygens.tei.handlers.XmlTextHandler;

public class SubresourceElementVisitor extends ExportVisitor {
  private List<String> subresourceElements;

  public SubresourceElementVisitor(List<String> subresourceElements) {
    this.subresourceElements = subresourceElements;
    setCommentHandler(new DefaultCommentHandler<>());
    setTextHandler(new XmlTextHandler<>());
    setDefaultElementHandler(new RenderElementHandler());
    setProcessingInstructionHandler(new DefaultProcessingInstructionHandler<>());
  }

  public String getBaseText() {
    return getContext().getResult();
  }

}
