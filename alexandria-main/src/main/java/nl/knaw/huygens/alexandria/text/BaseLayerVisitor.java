package nl.knaw.huygens.alexandria.text;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import nl.knaw.huygens.alexandria.model.BaseLayerDefinition;
import nl.knaw.huygens.tei.Comment;
import nl.knaw.huygens.tei.CommentHandler;
import nl.knaw.huygens.tei.Element;
import nl.knaw.huygens.tei.ElementHandler;
import nl.knaw.huygens.tei.Traversal;
import nl.knaw.huygens.tei.XmlContext;
import nl.knaw.huygens.tei.export.ExportVisitor;
import nl.knaw.huygens.tei.handlers.XmlTextHandler;

public class BaseLayerVisitor extends ExportVisitor implements CommentHandler<XmlContext>, ElementHandler<XmlContext> {
  List<AnnotationData> annotationData = new ArrayList<>();
  private UUID baselayerDefiningResourceId;
  static List<String> annotationActions = new ArrayList<>();

  public static List<String> getAnnotationActions() {
    return annotationActions;
  }

  public BaseLayerVisitor(BaseLayerDefinition baseLayerDefinition) {
    super();
    setCommentHandler(this);
    setTextHandler(new XmlTextHandler<>());
    setDefaultElementHandler(this);
    baseLayerDefinition.getBaseElementDefinitions().forEach(bed -> {
      addElementHandler(new BaseElementHandler(bed.getBaseAttributes()), bed.getName());
    });
  }

  @Override
  public Traversal enterElement(Element element, XmlContext context) {
    context.openLayer();
    return Traversal.NEXT;
  }

  @Override
  public Traversal leaveElement(Element element, XmlContext context) {
    String annotatedBaseText = context.closeLayer();
    context.addLiteral(annotatedBaseText);
    annotationActions.add("element annotation on '" + annotatedBaseText + "':element=" + element.getName() + element.getAttributes());
    // AnnotationData aData = new AnnotationData();
    // aData.setAnnotatedBaseText(annotatedBaseText);
    // aData.setLevel(XmlAnnotationLevel.element);
    // aData.setType(element.getName());
    // aData.setValue(element);
    return Traversal.NEXT;
  }

  @Override
  public Traversal visitComment(Comment element, XmlContext context) {
    return Traversal.NEXT;
  }

  static class BaseElementHandler implements ElementHandler<XmlContext> {
    private List<String> baseAttributes;

    public BaseElementHandler(List<String> baseAttributes) {
      this.baseAttributes = baseAttributes;
    }

    @Override
    public Traversal enterElement(Element element, XmlContext context) {
      Element base = new Element(element.getName());
      element.getAttributes().forEach((key, value) -> {
        if (baseAttributes.contains(key)) {
          base.setAttribute(key, value);
        } else {
          // TODO: annotation
          annotationActions.add("attribute annotation on '" + element.getName() + "':" + key + "=" + value);
        }
      });
      context.addOpenTag(base);
      return Traversal.NEXT;
    }

    @Override
    public Traversal leaveElement(Element element, XmlContext context) {
      context.addCloseTag(element);
      return Traversal.NEXT;
    }
  }

  public String getBaseLayerData() {
    return getContext().getResult();
  }

}
