package nl.knaw.huygens.alexandria.service;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.tinkerpop.gremlin.process.traversal.Order;
import org.apache.tinkerpop.gremlin.process.traversal.Traverser;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import nl.knaw.huygens.Log;
import nl.knaw.huygens.alexandria.api.model.text.TextRangeAnnotation;
import nl.knaw.huygens.alexandria.storage.EdgeLabels;
import nl.knaw.huygens.alexandria.storage.Storage;
import nl.knaw.huygens.alexandria.storage.VertexLabels;
import nl.knaw.huygens.alexandria.storage.frames.TextRangeAnnotationVF;
import nl.knaw.huygens.alexandria.textgraph.ParseResult;
import nl.knaw.huygens.alexandria.textgraph.TextAnnotation;
import nl.knaw.huygens.alexandria.textgraph.TextGraphSegment;
import nl.knaw.huygens.alexandria.textgraph.XmlAnnotation;
import nl.knaw.huygens.alexandria.util.StreamUtil;
import peapod.FramedGraphTraversal;

public class TextGraphService {
  private static Storage storage;

  public TextGraphService(Storage storage) {
    TextGraphService.storage = storage;
  }

  public void storeTextGraph(UUID resourceId, ParseResult result) {
    storage.runInTransaction(() -> {
      Vertex resource = getResourceVertex(resourceId);
      Vertex text = storage.addVertex(T.label, VertexLabels.TEXTGRAPH);
      resource.addEdge(EdgeLabels.HAS_TEXTGRAPH, text);
      resource.property("hasText", true);
      List<Vertex> textSegments = storeTextSegments(result.getTextSegments(), text);
      storeTextAnnotations(result.getXmlAnnotations(), text, textSegments);
    });
  }

  public Stream<TextGraphSegment> getTextGraphSegmentStream(UUID resourceId) {
    Iterator<Vertex> textSegmentIterator = new Iterator<Vertex>() {
      Vertex textSegment = getVertexTraversalFromResource(resourceId)//
          .out(EdgeLabels.HAS_TEXTGRAPH)//
          .out(EdgeLabels.FIRST_TEXT_SEGMENT)//
          .next();// because there can only be one

      @Override
      public boolean hasNext() {
        return textSegment != null;
      }

      @Override
      public Vertex next() {
        Vertex next = textSegment;
        Iterator<Vertex> nextVertices = textSegment.vertices(Direction.OUT, EdgeLabels.NEXT);
        textSegment = nextVertices.hasNext() ? nextVertices.next() : null;
        return next;
      }
    };

    return StreamUtil.stream(textSegmentIterator)//
        .map(this::toTextGraphSegment);
  }

  public Stream<TextAnnotation> getTextAnnotationStream(UUID resourceId) {
    return getTextAnnotationVertexStream(resourceId).map(this::toTextAnnotation);
  }

  private Vertex getResourceVertex(UUID resourceId) {
    return getVertexTraversalFromResource(resourceId).next();
  }

  private GraphTraversal<Vertex, Vertex> getVertexTraversalFromResource(UUID resourceId) {
    return storage.getResourceVertexTraversal()//
        .has(Storage.IDENTIFIER_PROPERTY, resourceId.toString());
  }

  private Stream<Vertex> getTextAnnotationVertexStream(UUID resourceId) {
    Iterator<Vertex> textAnnotationIterator = new Iterator<Vertex>() {
      Vertex textAnnotationVertex = getVertexTraversalFromResource(resourceId)//
          .out(EdgeLabels.HAS_TEXTGRAPH)//
          .out(EdgeLabels.FIRST_ANNOTATION)//
          .next();// because there can only be one

      @Override
      public boolean hasNext() {
        return textAnnotationVertex != null;
      }

      @Override
      public Vertex next() {
        Vertex next = textAnnotationVertex;
        Iterator<Vertex> nextVertices = textAnnotationVertex.vertices(Direction.OUT, EdgeLabels.NEXT);
        textAnnotationVertex = nextVertices.hasNext() ? nextVertices.next() : null;
        return next;
      }
    };
    Stream<Vertex> stream = StreamUtil.stream(textAnnotationIterator);
    return stream;
  }

  public void updateTextAnnotation(TextAnnotation textAnnotation) {
    Vertex vertex = getTextAnnotationVertex(textAnnotation);
    update(vertex, textAnnotation);
  }

  public void wrapContentInChildTextAnnotation(TextAnnotation existingTextAnnotation, TextAnnotation newTextAnnotation) {
    Vertex parentVertex = getTextAnnotationVertex(existingTextAnnotation);
    Iterator<Edge> parentOutEdges = parentVertex.edges(Direction.OUT, EdgeLabels.FIRST_TEXT_SEGMENT, EdgeLabels.LAST_TEXT_SEGMENT, EdgeLabels.NEXT);
    Vertex childVertex = toVertex(newTextAnnotation);

    // copy FIRST_TEXT_SEGMENT, LAST_TEXT_SEGMENT, NEXT edges from parentVertex to childVertex
    while (parentOutEdges.hasNext()) {
      Edge outEdge = parentOutEdges.next();
      childVertex.addEdge(outEdge.label(), outEdge.inVertex());
    }
    // remove existing NEXT edge for parentVertex, replace with NEXT edge pointing to childVertex
    Iterator<Edge> parentNextEdgeIterator = parentVertex.edges(Direction.OUT, EdgeLabels.NEXT);
    if (parentNextEdgeIterator.hasNext()) {
      Edge nextEdge = parentNextEdgeIterator.next();
      nextEdge.remove();
      parentVertex.addEdge(EdgeLabels.NEXT, childVertex);

      // increase the depth of the next textannotations as long as they point to the same textsegments
      Vertex firstTextSegment = firstTextSegment(parentVertex);
      Vertex lastTextSegment = lastTextSegment(parentVertex);
      boolean goOn = true;
      while (goOn) {
        Vertex next = nextTextAnnotation(childVertex);
        Vertex newFirst = firstTextSegment(next);
        Vertex newLast = lastTextSegment(next);
        if (newFirst.equals(firstTextSegment) && newLast.equals(lastTextSegment)) {
          int currentDepth = getIntValue(next, TextAnnotation.Properties.depth);
          next.property(TextAnnotation.Properties.depth, currentDepth + 1);
          childVertex = next;
          goOn = childVertex.edges(Direction.OUT, EdgeLabels.NEXT).hasNext();
        } else {
          goOn = false;
        }
      }
    }
  }

  public void updateTextAnnotationLink(TextRangeAnnotationVF vf, TextRangeAnnotation textRangeAnnotation, UUID resourceId) {
    // if the TextRangeAnnotationVF is already linked to a TextAnnotation, remove that TextAnnotation
    FramedGraphTraversal<TextRangeAnnotationVF, Vertex> traversal = vf.out(nl.knaw.huygens.alexandria.storage.frames.TextRangeAnnotationVF.EdgeLabels.HAS_TEXTANNOTATION);
    if (traversal.hasNext()) {
      traversal.next().remove();
    }

    Vertex newTextAnnotationVertex = createNewTextAnnotation(vf, textRangeAnnotation);

    TextAnnotationInsertionContext context = new TextAnnotationInsertionContext(newTextAnnotationVertex, textRangeAnnotation);
    List<Vertex> list = getVertexTraversalFromResource(resourceId)//
        .out(EdgeLabels.HAS_TEXTGRAPH)//
        .out(EdgeLabels.FIRST_ANNOTATION)//

        // find the TextAnnotation with the xml:id from annotation.position.xmlid
        .until(__.has(TextAnnotation.Properties.xmlid, textRangeAnnotation.getPosition().getXmlId()))//
        .repeat(__.out(EdgeLabels.NEXT))//
        .out(EdgeLabels.FIRST_TEXT_SEGMENT)//

        // find the textsegment where the textrange from annotation.position starts
        .until(context::rangeStartsInThisTextSegment)//
        .repeat(__.out(EdgeLabels.NEXT))//
        // insert the new TextAnnotation after the deepest TextAnnotation that has this as a FIRST_TEXT_SEGMENT
        .sideEffect(__.in(EdgeLabels.FIRST_TEXT_SEGMENT)//
            .has(TextAnnotation.Properties.depth) // TODO: find out why there are textAnnotations without properties
            .order().by(TextAnnotation.Properties.depth, Order.incr).tail().sideEffect(context::insertNewTextAnnotationVertex))
        .sideEffect(context::processFirstTextSegmentInRange)//

        // iterate over the next TextSegments until you find the one the textrange from annotation.position ends in
        .until(context::rangeEndsInThisTextSegment)//
        .repeat(__.out(EdgeLabels.NEXT))//
        .sideEffect(context::processLastTextSegmentInRange)//

        .toList();
    if (list.size() != 1) {
      Log.error("listsize should be 1, is {}", list.size());
    }
  }

  private Vertex createNewTextAnnotation(TextRangeAnnotationVF textRangeAnnotationVF, TextRangeAnnotation textRangeAnnotation) {
    // create new TextAnnotation
    Map<String, String> attributes = ImmutableMap.of(TextRangeAnnotation.RESPONSIBILITY_ATTRIBUTE, "#" + textRangeAnnotation.getAnnotator());
    TextAnnotation newTextAnnotation = new TextAnnotation(textRangeAnnotation.getName(), attributes, 10);
    Vertex newTextAnnotationVertex = toVertex(newTextAnnotation);

    // link TextAnnotation to TextRangeAnnotation
    textRangeAnnotationVF.vertex().addEdge(nl.knaw.huygens.alexandria.storage.frames.TextRangeAnnotationVF.EdgeLabels.HAS_TEXTANNOTATION, newTextAnnotationVertex);
    return newTextAnnotationVertex;
  }

  private static class TextAnnotationInsertionContext {
    private int textSize;
    private int rangeStart;
    private int rangeEnd;
    private Vertex newTextAnnotationVertex;
    private Vertex lastTextSegmentVertex;

    public TextAnnotationInsertionContext(Vertex newTextAnnotationVertex, TextRangeAnnotation textRangeAnnotation) {
      this.newTextAnnotationVertex = newTextAnnotationVertex;
      this.textSize = 0;
      this.rangeStart = textRangeAnnotation.getPosition().getOffset();
      this.rangeEnd = this.rangeStart + textRangeAnnotation.getPosition().getLength() - 1;
      Log.info("range = [{},{}]", rangeStart, rangeEnd);
    }

    void insertNewTextAnnotationVertex(Traverser<Vertex> t) {
      Vertex deepestTextAnnotationVertex = t.get();
      checkVertexLabel(deepestTextAnnotationVertex, VertexLabels.TEXTANNOTATION);
      int depth = getIntValue(deepestTextAnnotationVertex, TextAnnotation.Properties.depth);
      newTextAnnotationVertex.property(TextAnnotation.Properties.depth, depth + 1);
      Vertex nextTextAnnotation = storage.getVertexTraversal(deepestTextAnnotationVertex.id())//
          .out(EdgeLabels.NEXT).next();
      storage.getVertexTraversal(deepestTextAnnotationVertex.id())//
          .outE(EdgeLabels.NEXT).next().remove();
      deepestTextAnnotationVertex.addEdge(EdgeLabels.NEXT, newTextAnnotationVertex);
      newTextAnnotationVertex.addEdge(EdgeLabels.NEXT, nextTextAnnotation);

      Log.info("textAnnotation.name={}", deepestTextAnnotationVertex.value(TextAnnotation.Properties.name).toString());
    }

    boolean rangeStartsInThisTextSegment(Traverser<Vertex> t) {
      Log.info("rangeStartsInThisTextSegment");
      incTextSize(t);
      return textSize >= rangeStart;
    }

    void processFirstTextSegmentInRange(Traverser<Vertex> t) {
      Log.info("processFirstTextSegmentInRange");
      Vertex textSegmentVertex = t.get();
      checkVertexLabel(textSegmentVertex, VertexLabels.TEXTSEGMENT);

      // if needed, split up the textsegment, preserving the TextAnnotation links
      int tailLength = textSize - rangeStart + 1;
      Log.info("textSize = {}, tailLength = {}", textSize, tailLength);
      // link the new TextAnnotation to the tail if detaching was necessary, to the firstTextSegment otherwise
      if (tailLength > 0) {
        Vertex newTail = detachTail(textSegmentVertex, tailLength);
        newTextAnnotationVertex.addEdge(EdgeLabels.FIRST_TEXT_SEGMENT, newTail);

      } else {
        newTextAnnotationVertex.addEdge(EdgeLabels.FIRST_TEXT_SEGMENT, textSegmentVertex);
      }
    }

    boolean rangeEndsInThisTextSegment(Traverser<Vertex> t) {
      Log.info("rangeEndsInThisTextSegment");
      incTextSize(t);
      return textSize >= rangeEnd;
    }

    void processLastTextSegmentInRange(Traverser<Vertex> t) {
      Log.info("processLastTextSegmentInRange");
      Vertex textSegmentVertex = t.get();
      checkVertexLabel(textSegmentVertex, VertexLabels.TEXTSEGMENT);

      // if needed, split up the textsegment, preserving the TextAnnotation links
      int tailLength = textSize - rangeEnd;
      Log.info("textSize = {}, tailLength = {}", textSize, tailLength);

      // link the new TextAnnotation to the head if detaching was necessary, to the lastTextSegment otherwise
      if (tailLength > 0) {
        Vertex newHead = detachHead(textSegmentVertex, tailLength);
        newTextAnnotationVertex.addEdge(EdgeLabels.LAST_TEXT_SEGMENT, newHead);
      } else {
        newTextAnnotationVertex.addEdge(EdgeLabels.LAST_TEXT_SEGMENT, textSegmentVertex);
      }
    }

    private void incTextSize(Traverser<Vertex> t) {
      Vertex textSegmentVertex = t.get();
      checkVertexLabel(textSegmentVertex, VertexLabels.TEXTSEGMENT);
      String text = getStringValue(textSegmentVertex, TextSegment.Properties.text);
      Log.info("text={}", text);
      textSize += text.length();
      lastTextSegmentVertex = textSegmentVertex;
    }

    private Vertex detachTail(Vertex textSegment, int tailLength) {
      Preconditions.checkArgument(textSegment.label().equals(VertexLabels.TEXTSEGMENT));
      String text = getStringValue(textSegment, TextSegment.Properties.text);
      int length = text.length();
      textSize = textSize - length;
      int headLength = length - tailLength;
      String headText = text.substring(0, headLength);
      String tailText = text.substring(headLength);
      Log.info("head = [{}], tail = [{}]", headText, tailText);
      textSegment.property(TextSegment.Properties.text, headText);
      Vertex tailTextSegment = newTextSegmentVertex(tailText);
      // move LAST_TEXT_SEGMENT edges to tailTextSegment
      insertNewAfterCurrent(textSegment, tailTextSegment);
      StreamUtil.parallelStream(textSegment.edges(Direction.IN, EdgeLabels.LAST_TEXT_SEGMENT))//
          .forEach(e -> {
            Vertex textAnnotationVertex = e.outVertex();
            textAnnotationVertex.addEdge(EdgeLabels.LAST_TEXT_SEGMENT, tailTextSegment);
            e.remove();
          });
      return tailTextSegment;
    }

    private void insertNewAfterCurrent(Vertex currentVertex, Vertex newVertex) {
      Iterator<Edge> edges = currentVertex.edges(Direction.OUT, EdgeLabels.NEXT);
      if (edges.hasNext()) {
        Edge nextEdge = edges.next();
        Vertex nextVertex = nextEdge.inVertex();
        nextEdge.remove();
        newVertex.addEdge(EdgeLabels.NEXT, nextVertex);
      }
      currentVertex.addEdge(EdgeLabels.NEXT, newVertex);
    }

    private Vertex detachHead(Vertex textSegment, int tailLength) {
      Preconditions.checkArgument(textSegment.label().equals(VertexLabels.TEXTSEGMENT));
      String text = getStringValue(textSegment, TextSegment.Properties.text);
      int length = text.length();
      String headText = text.substring(0, length - tailLength);
      String tailText = text.substring(length - tailLength);
      Log.info("head = [{}], tail = [{}]", headText, tailText);
      textSegment.property(TextSegment.Properties.text, tailText);
      Vertex headTextSegment = newTextSegmentVertex(headText);
      // move FIRST_TEXT_SEGMENT edges to tailTextSegment
      insertNewBeforeCurrent(textSegment, headTextSegment);
      StreamUtil.parallelStream(textSegment.edges(Direction.IN, EdgeLabels.FIRST_TEXT_SEGMENT))//
          .forEach(e -> {
            Vertex textAnnotationVertex = e.outVertex();
            textAnnotationVertex.addEdge(EdgeLabels.FIRST_TEXT_SEGMENT, headTextSegment);
            e.remove();
          });
      return headTextSegment;
    }

    private void insertNewBeforeCurrent(Vertex currentVertex, Vertex newVertex) {
      Iterator<Edge> edges = currentVertex.edges(Direction.IN, EdgeLabels.NEXT);
      if (edges.hasNext()) {
        Edge prevEdge = edges.next();
        Vertex prevVertex = prevEdge.outVertex();
        prevEdge.remove();
        prevVertex.addEdge(EdgeLabels.NEXT, newVertex);
      }
      newVertex.addEdge(EdgeLabels.NEXT, currentVertex);
    }

  }

  // private methods //

  private static int getIntValue(Vertex vertex, String propertyName) {
    return (int) vertex.value(propertyName);
  }

  private static String getStringValue(Vertex vertex, String propertyName) {
    return (String) vertex.value(propertyName);
  }

  private Vertex nextTextAnnotation(Vertex childVertex) {
    return childVertex.vertices(Direction.OUT, EdgeLabels.NEXT).next();
  }

  private Vertex lastTextSegment(Vertex parentVertex) {
    return parentVertex.vertices(Direction.OUT, EdgeLabels.LAST_TEXT_SEGMENT).next();
  }

  private Vertex firstTextSegment(Vertex parentVertex) {
    return parentVertex.vertices(Direction.OUT, EdgeLabels.FIRST_TEXT_SEGMENT).next();
  }

  private Vertex getTextAnnotationVertex(TextAnnotation textAnnotation) {
    Object id = textAnnotation.getId();
    Vertex vertex = storage.getVertexTraversal(id).next();
    return vertex;
  }

  private List<Vertex> storeTextSegments(List<String> textSegments, Vertex text) {
    List<Vertex> textSegmentVertices = new ArrayList<>();
    Vertex previous = null;
    for (String s : textSegments) {
      Vertex v = newTextSegmentVertex(s);
      if (previous == null) {
        text.addEdge(EdgeLabels.FIRST_TEXT_SEGMENT, v);
      } else {
        previous.addEdge(EdgeLabels.NEXT, v);
      }
      textSegmentVertices.add(v);
      previous = v;
    }
    return textSegmentVertices;
  }

  private void storeTextAnnotations(Set<XmlAnnotation> xmlAnnotations, Vertex text, List<Vertex> textSegments) {
    Vertex previous = null;
    for (XmlAnnotation xmlAnnotation : xmlAnnotations) {
      Vertex v = toVertex(xmlAnnotation);
      v.addEdge(EdgeLabels.FIRST_TEXT_SEGMENT, textSegments.get(xmlAnnotation.getFirstSegmentIndex()));
      v.addEdge(EdgeLabels.LAST_TEXT_SEGMENT, textSegments.get(xmlAnnotation.getLastSegmentIndex()));
      if (previous == null) {
        text.addEdge(EdgeLabels.FIRST_ANNOTATION, v);
      } else {
        previous.addEdge(EdgeLabels.NEXT, v);
      }
      previous = v;
    }
  }

  private static Vertex toVertex(TextAnnotation textAnnotation) {
    Vertex v = storage.addVertex(T.label, VertexLabels.TEXTANNOTATION);
    update(v, textAnnotation);
    return v;
  }

  private static void update(Vertex v, TextAnnotation textAnnotation) {
    Map<String, String> attributes = textAnnotation.getAttributes();
    String[] attributeKeys = new String[attributes.size()];
    String[] attributeValues = new String[attributes.size()];
    int i = 0;
    for (Entry<String, String> kv : attributes.entrySet()) {
      attributeKeys[i] = kv.getKey();
      attributeValues[i] = kv.getValue();
      i++;
    }
    v.property(TextAnnotation.Properties.name, textAnnotation.getName());
    v.property(TextAnnotation.Properties.attribute_keys, attributeKeys);
    v.property(TextAnnotation.Properties.attribute_values, attributeValues);
    v.property(TextAnnotation.Properties.depth, textAnnotation.getDepth());
    if (attributes.containsKey("xml:id")) {
      v.property(TextAnnotation.Properties.xmlid, attributes.get("xml:id"));
    }
  }

  private TextGraphSegment toTextGraphSegment(Vertex textSegment) {
    TextGraphSegment textGraphSegment = new TextGraphSegment();
    if (textSegment.keys().contains(TextSegment.Properties.text)) {
      textGraphSegment.setTextSegment(textSegment.value(TextSegment.Properties.text));
    }
    List<TextAnnotation> textAnnotationsToOpen = getTextAnnotationsToOpen(textSegment);
    List<TextAnnotation> textAnnotationsToClose = getTextAnnotationsToClose(textSegment);

    if (StringUtils.isEmpty(textGraphSegment.getTextSegment())//
        && !textAnnotationsToOpen.isEmpty()//
        && !textAnnotationsToClose.isEmpty()//
    ) {
      TextAnnotation lastToOpen = textAnnotationsToOpen.get(textAnnotationsToOpen.size() - 1);
      TextAnnotation firstToClose = textAnnotationsToClose.get(0);
      if (lastToOpen.equals(firstToClose)) {
        textAnnotationsToOpen.remove(lastToOpen);
        textGraphSegment.setMilestoneAnnotation(lastToOpen);
        textAnnotationsToClose.remove(firstToClose);
      }
    }
    textGraphSegment.setAnnotationsToOpen(textAnnotationsToOpen);
    textGraphSegment.setAnnotationsToClose(textAnnotationsToClose);
    return textGraphSegment;
  }

  private static final Comparator<TextAnnotation> BY_INCREASING_DEPTH = (e1, e2) -> e1.getDepth().compareTo(e2.getDepth());

  private List<TextAnnotation> getTextAnnotationsToOpen(Vertex textSegment) {
    return getTextAnnotations(textSegment, EdgeLabels.FIRST_TEXT_SEGMENT);
  }

  private List<TextAnnotation> getTextAnnotationsToClose(Vertex textSegment) {
    return Lists.reverse(getTextAnnotations(textSegment, EdgeLabels.LAST_TEXT_SEGMENT));
  }

  private List<TextAnnotation> getTextAnnotations(Vertex textSegment, String edgeLabel) {
    return StreamUtil.stream(textSegment.vertices(Direction.IN, edgeLabel))//
        .filter(v -> v.label().equals(VertexLabels.TEXTANNOTATION))// this filter should not be necessary
        .map(this::toTextAnnotation)//
        .sorted(BY_INCREASING_DEPTH)//
        .collect(toList());
  }

  private TextAnnotation toTextAnnotation(Vertex vertex) {
    Map<String, String> attributes = getAttributeMap(vertex);
    TextAnnotation textAnnotation = new TextAnnotation(//
        vertex.value(TextAnnotation.Properties.name), //
        attributes, //
        vertex.value(TextAnnotation.Properties.depth)//
    );
    textAnnotation.setId(vertex.id());
    return textAnnotation;
  }

  private Map<String, String> getAttributeMap(Vertex vertex) {
    Map<String, String> attributes = Maps.newLinkedHashMap();
    if (vertex.keys().contains(TextAnnotation.Properties.attribute_keys)) {
      String[] keys = vertex.value(TextAnnotation.Properties.attribute_keys);
      String[] values = vertex.value(TextAnnotation.Properties.attribute_values);
      for (int i = 0; i < keys.length; i++) {
        attributes.put(keys[i], values[i]);
      }
    }
    return attributes;
  }

  private static Vertex newTextSegmentVertex(String s) {
    return storage.addVertex(T.label, VertexLabels.TEXTSEGMENT, TextSegment.Properties.text, s);
  }

  private static void checkVertexLabel(Vertex vertex, String label) {
    if (!vertex.label().equals(label)) {
      throw new IllegalArgumentException("vertex label should be '" + label + "', but is '" + vertex.label() + "'.");
    }
  }

}
