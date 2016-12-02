package nl.knaw.huygens.alexandria.service;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.tinkerpop.gremlin.process.traversal.Order;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.Traverser;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.VertexProperty;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import nl.knaw.huygens.Log;
import nl.knaw.huygens.alexandria.api.model.text.TextRangeAnnotation;
import nl.knaw.huygens.alexandria.api.model.text.TextRangeAnnotation.AbsolutePosition;
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

  public void storeTextGraph(UUID resourceUUID, ParseResult result) {
    storage.runInTransaction(() -> {
      Vertex resource = getResourceVertex(resourceUUID);
      Vertex text = storage.addVertex(T.label, VertexLabels.TEXTGRAPH);
      resource.addEdge(EdgeLabels.HAS_TEXTGRAPH, text);
      resource.property("hasText", true);
      List<Vertex> textSegments = storeTextSegments(result.getTextSegments(), text);
      storeTextAnnotations(result.getXmlAnnotations(), text, textSegments);
      reindex(resourceUUID);
    });
  }

  public Stream<TextGraphSegment> getTextGraphSegmentStream(UUID resourceUUID) {
    return getTextSegmentVertexStream(resourceUUID)//
        .map(this::toTextGraphSegment);
  }

  public Stream<TextAnnotation> getTextAnnotationStream(UUID resourceUUID) {
    return getTextAnnotationVertexStream(resourceUUID).map(this::toTextAnnotation);
  }

  private Vertex getResourceVertex(UUID resourceUUID) {
    return getVertexTraversalFromResource(resourceUUID).next();
  }

  private GraphTraversal<Vertex, Vertex> getVertexTraversalFromResource(UUID resourceUUID) {
    return storage.getResourceVertexTraversal()//
        .has(Storage.IDENTIFIER_PROPERTY, resourceUUID.toString());
  }

  public Stream<Vertex> getTextSegmentVertexStream(UUID resourceUUID) {
    Iterator<Vertex> textSegmentIterator = new Iterator<Vertex>() {
      Vertex textSegment = getVertexTraversalFromResource(resourceUUID)//
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

    return StreamUtil.stream(textSegmentIterator);
  }

  private Stream<Vertex> getTextAnnotationVertexStream(UUID resourceUUID) {
    Iterator<Vertex> textAnnotationIterator = new Iterator<Vertex>() {
      Vertex textAnnotationVertex = getVertexTraversalFromResource(resourceUUID)//
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
    return StreamUtil.stream(textAnnotationIterator);
  }

  public void updateTextAnnotation(TextAnnotation textAnnotation) {
    Vertex vertex = getTextAnnotationVertex(textAnnotation);
    update(vertex, textAnnotation);
  }

  public void wrapContentInChildTextAnnotation(TextAnnotation parentTextAnnotation, TextAnnotation newChildTextAnnotation) {
    Vertex parentVertex = getTextAnnotationVertex(parentTextAnnotation);
    Iterator<Edge> parentOutEdges = parentVertex.edges(Direction.OUT, EdgeLabels.FIRST_TEXT_SEGMENT, EdgeLabels.LAST_TEXT_SEGMENT, EdgeLabels.NEXT);
    Vertex childVertex = toVertex(newChildTextAnnotation);

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

      // increase the depth of the next textannotations as long as the textrange they annotate overlaps with the textrange of the parent annotation
      updateDepths(parentVertex, childVertex, parentTextAnnotation.getDepth());
    }
  }

  private static void updateDepths(Vertex parentVertex, Vertex childVertex, int parentDepth) {
    // TODO: use start/end textSegmentIndex to determine the relevant annotations to adjust.
    Vertex firstTextSegment = firstTextSegment(parentVertex);
    Vertex lastTextSegment = lastTextSegment(parentVertex);
    Set<Vertex> updatedVertices = Sets.newHashSet();
    updatedVertices.add(parentVertex);
    updatedVertices.add(childVertex);
    boolean goOn = true;
    Vertex textSegment = firstTextSegment;
    while (goOn) {
      StreamUtil.stream(textSegment.vertices(Direction.IN, EdgeLabels.FIRST_TEXT_SEGMENT))//
          .filter(v -> VertexLabels.TEXTANNOTATION.equals(v.label()))//
          .filter(v -> !updatedVertices.contains(v))//
          .forEach(v -> {
            // Log.debug("v={}", v);
            // Log.debug("updatedVertices={}", updatedVertices);
            int currentDepth = getIntValue(v, TextAnnotation.Properties.depth);
            if (currentDepth > parentDepth) {
              v.property(TextAnnotation.Properties.depth, currentDepth + 1);
              updatedVertices.add(v);
            }
          });
      goOn = !(textSegment.equals(lastTextSegment));
      if (goOn) {
        Iterator<Vertex> nextTextSegment = textSegment.vertices(Direction.OUT, EdgeLabels.NEXT);
        if (nextTextSegment.hasNext()) {
          textSegment = nextTextSegment.next();
        } else {
          Log.error("There seems to be something wrong with the graph.");
          goOn = false;
        }
      }
    }
  }

  public void updateTextAnnotationLink(TextRangeAnnotationVF vf, TextRangeAnnotation textRangeAnnotation, UUID resourceUUID) {
    // Log.debug("textRangeAnnotation={}", textRangeAnnotation);
    // if the TextRangeAnnotationVF is already linked to a TextAnnotation, remove that TextAnnotation
    FramedGraphTraversal<TextRangeAnnotationVF, Vertex> traversal = vf.out(nl.knaw.huygens.alexandria.storage.frames.TextRangeAnnotationVF.EdgeLabels.HAS_TEXTANNOTATION);
    if (traversal.hasNext()) {
      traversal.next().remove();
    }

    Vertex newTextAnnotationVertex = createNewTextAnnotation(vf, textRangeAnnotation);
    // Log.debug("textRangeAnnotation={}", textRangeAnnotation);

    TextAnnotationInsertionContext context = new TextAnnotationInsertionContext(newTextAnnotationVertex, textRangeAnnotation);
    GraphTraversal<Vertex, Vertex> firstTextSegmentTraversal = getVertexTraversalFromResource(resourceUUID)//
        .out(EdgeLabels.HAS_TEXTGRAPH)//
        .out(EdgeLabels.FIRST_ANNOTATION)//

        // find the TextAnnotation with the xml:id from annotation.position.xmlid
        .until(__.has(TextAnnotation.Properties.xmlid, textRangeAnnotation.getAbsolutePosition().getXmlId()))//
        .repeat(__.out(EdgeLabels.NEXT))//
        .out(EdgeLabels.FIRST_TEXT_SEGMENT);
    if (context.annotationIsMilestone) {
      handleMilestoneAnnotation(newTextAnnotationVertex, context, firstTextSegmentTraversal);
    } else {
      handleRegularAnnotation(resourceUUID, context, firstTextSegmentTraversal);
    }
    if (context.reindexNeeded) {
      reindex(resourceUUID);
    }
    context.insertNewTextAnnotationVertex();
    reindex(resourceUUID);
    Log.debug("newTextAnnotationVertex={}", context.visualizeVertex(newTextAnnotationVertex));
  }

  private void handleRegularAnnotation(UUID resourceUUID, TextAnnotationInsertionContext context, GraphTraversal<Vertex, Vertex> traversal2) {
    List<Vertex> list = traversal2//
        // find the textsegment where the textrange from annotation.position starts
        .until(context::rangeStartsInThisTextSegment)//
        .repeat(__.out(EdgeLabels.NEXT))//
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

  private void handleMilestoneAnnotation(Vertex newTextAnnotationVertex, TextAnnotationInsertionContext context, GraphTraversal<Vertex, Vertex> firstTextSegmentTraversal) {
    // Milestones should be linked to their own empty TextSegment
    Vertex emptyTextSegment = newTextSegmentVertex("");
    context.linkTextAnnotationToTextSegment(newTextAnnotationVertex, emptyTextSegment);

    // now we have to fit in this new textSegment
    int startOffset = 0;
    int endOffset = 0;
    Vertex textSegment = firstTextSegmentTraversal.next();
    Log.info("textSegment={}", context.visualizeVertex(textSegment));

    // find the textsegment where the textrange from annotation.position starts
    boolean goOn = true;
    while (goOn) {
      String text = getStringValue(textSegment, TextSegment.Properties.text);
      int segmentSize = text.length();
      goOn = startOffset + segmentSize < context.rangeStart;
      Log.debug("segmentSize={}", segmentSize);
      endOffset += segmentSize;
      if (goOn) {
        Iterator<Vertex> nextTextSegments = textSegment.vertices(Direction.OUT, EdgeLabels.NEXT);
        if (nextTextSegments.hasNext()) {
          startOffset += segmentSize;
          textSegment = nextTextSegments.next();
          Log.info("textSegment={}", context.visualizeVertex(textSegment));
        } else {
          goOn = false;
        }
      }
    }
    Log.debug("startOffset {}; endOffset {}; rangeStart {}; rangeEnd {}", startOffset, endOffset, context.rangeStart, context.rangeEnd);
    if (startOffset == context.rangeStart) {
      Log.debug("insertBefore!");
      context.insertNewBeforeCurrent(emptyTextSegment, textSegment);
      StreamUtil.stream(textSegment.edges(Direction.IN, EdgeLabels.FIRST_TEXT_SEGMENT))//
          .forEach(e -> context.moveEdge(e, EdgeLabels.FIRST_TEXT_SEGMENT, emptyTextSegment));

    } else if (endOffset == context.rangeEnd) {
      Log.debug("insertAfter!");
      context.insertNewAfterCurrent(emptyTextSegment, textSegment);
      StreamUtil.stream(textSegment.edges(Direction.IN, EdgeLabels.LAST_TEXT_SEGMENT))//
          .forEach(e -> context.moveEdge(e, EdgeLabels.LAST_TEXT_SEGMENT, emptyTextSegment));

    } else {
      // split up textSegment?
      Log.debug("somewhere inbetween?");

    }
    Log.info("emptyTextSegment={}", context.visualizeVertex(emptyTextSegment));

    context.reindexNeeded = true;
  }

  private Vertex createNewTextAnnotation(TextRangeAnnotationVF textRangeAnnotationVF, TextRangeAnnotation textRangeAnnotation) {
    // create new TextAnnotation
    Map<String, String> attributes = ImmutableMap.<String, String> builder()//
        .putAll(textRangeAnnotation.getAttributes())//
        .put(TextRangeAnnotation.RESPONSIBILITY_ATTRIBUTE, "#" + textRangeAnnotation.getAnnotator())//
        .build();
    TextAnnotation newTextAnnotation = new TextAnnotation(textRangeAnnotation.getName(), attributes, 1000); // adjust depth once place in textannotationlist has been determined
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
    private Vertex startingTextSegment;
    private Vertex endingTextSegment;
    private boolean reindexNeeded = false;
    private boolean useOffset;
    private String parentXmlId;
    private boolean annotationIsMilestone = false;

    public TextAnnotationInsertionContext(Vertex newTextAnnotationVertex, TextRangeAnnotation textRangeAnnotation) {
      this.newTextAnnotationVertex = newTextAnnotationVertex;
      // Log.debug("textRangeAnnotation={}", textRangeAnnotation);
      this.textSize = 0;
      this.useOffset = textRangeAnnotation.getUseOffset();
      AbsolutePosition absolutePosition = textRangeAnnotation.getAbsolutePosition();
      this.parentXmlId = absolutePosition.getXmlId();
      this.rangeStart = absolutePosition.getOffset();
      Integer length = absolutePosition.getLength();
      this.rangeEnd = this.rangeStart + length - 1;
      if (this.rangeEnd == 0) {
        this.rangeStart = 0;
      }
      this.annotationIsMilestone = length == 0;
      // Log.debug("range = [{},{}]", rangeStart, rangeEnd);
    }

    private void linkTextAnnotationToTextSegment(Vertex newTextAnnotationVertex, Vertex emptyTextSegment) {
      this.startingTextSegment = emptyTextSegment;
      this.endingTextSegment = emptyTextSegment;
      newTextAnnotationVertex.addEdge(EdgeLabels.FIRST_TEXT_SEGMENT, this.startingTextSegment);
      newTextAnnotationVertex.addEdge(EdgeLabels.LAST_TEXT_SEGMENT, this.endingTextSegment);
    }

    void insertNewTextAnnotationVertex() {
      // Log.info("startingTextSegment:{}", visualizeVertex(startingTextSegment));
      if (useOffset) {
        insertUsingOffset();

      } else {
        // then straight after the parent annotation
        insertAfterParent();
      }
    }

    private void insertAfterParent() {
      // Log.debug("startingTextSegment={}", visualizeVertex(startingTextSegment));
      Vertex parentVertex = storage.getVertexTraversal(startingTextSegment)//
          .in(EdgeLabels.FIRST_TEXT_SEGMENT)//
          .hasLabel(VertexLabels.TEXTANNOTATION)//
          .has(TextAnnotation.Properties.xmlid, parentXmlId)//
          .next();
      // Log.debug("parentVertex={}", visualizeVertex(parentVertex));
      Iterator<Edge> edges = parentVertex.edges(Direction.OUT, EdgeLabels.NEXT);
      if (edges.hasNext()) {
        Edge oldNextEdge = edges.next();
        Vertex nextTextAnnotation = oldNextEdge.inVertex();
        newTextAnnotationVertex.addEdge(EdgeLabels.NEXT, nextTextAnnotation);
        oldNextEdge.remove();
      }
      parentVertex.addEdge(EdgeLabels.NEXT, newTextAnnotationVertex);
      int parentDepth = getDepth(parentVertex);
      newTextAnnotationVertex.property(TextAnnotation.Properties.depth, parentDepth + 1);
      updateDepths(parentVertex, newTextAnnotationVertex, parentDepth);
    }

    private void insertUsingOffset() {
      // int startingTextSegmentIndex = getIntValue(startingTextSegment, TextAnnotation.Properties.index);
      int endingTextSegmentIndex = getIntValue(endingTextSegment, TextAnnotation.Properties.index);
      // Log.debug("startIndex,endIndex=({},{})", startingTextSegmentIndex, endingTextSegmentIndex);
      Vertex parentTextAnnotationVertex = null;
      Vertex textSegment = startingTextSegment;
      while (parentTextAnnotationVertex == null) {
        GraphTraversal<Vertex, Vertex> tailTraversal = storage.getVertexTraversal(textSegment)//
            // find TextAnnotations that start here
            .in(EdgeLabels.FIRST_TEXT_SEGMENT)//
            .hasLabel(VertexLabels.TEXTANNOTATION)//
            // that are not the newTextAnnotation
            .not(__.hasId(newTextAnnotationVertex.id()))//
            // and that don't end before endingTextSegment
            .not(__.out(EdgeLabels.LAST_TEXT_SEGMENT).has(TextSegment.Properties.index, P.lt(endingTextSegmentIndex)))//
            // sort by depth
            .order().by(TextAnnotation.Properties.depth, Order.incr)//
            // get the deepest
            .tail();
        if (tailTraversal.hasNext()) {
          // We found the parent!
          parentTextAnnotationVertex = tailTraversal.next();
        } else {
          // go to the previous textSegment, and start over
          textSegment = textSegment.vertices(Direction.IN, EdgeLabels.NEXT).next();
        }
      }

      int parentDepth = getDepth(parentTextAnnotationVertex);
      setDepth(newTextAnnotationVertex, parentDepth + 1);
      updateDepths(parentTextAnnotationVertex, newTextAnnotationVertex, parentDepth);
      // Iterator<Edge> nextEdges = parentTextAnnotationVertex.edges(Direction.OUT, EdgeLabels.NEXT);
      // if (nextEdges.hasNext()) {
      // Edge next = nextEdges.next();
      // Vertex nextTextAnnotation = next.inVertex();
      // newTextAnnotationVertex.addEdge(EdgeLabels.NEXT, nextTextAnnotation);
      // next.remove();
      // }
      // parentTextAnnotationVertex.addEdge(EdgeLabels.NEXT, newTextAnnotationVertex);
      //
      // Iterator<Vertex> vertices = startingTextSegment.vertices(Direction.IN, EdgeLabels.FIRST_TEXT_SEGMENT);
      // StreamUtil.stream(vertices)//
      // .filter(v -> v.label().equals(VertexLabels.TEXTANNOTATION))//
      // .filter(v -> !v.equals(newTextAnnotationVertex))//
      // .filter(v -> getDepth(v) > parentDepth)//
      // .forEach(this::incrementDepth);
    }

    // private void incrementDepth(Vertex v) {
    // int currentDepth = getDepth(v);
    // setDepth(v, currentDepth + 1);
    // }

    private VertexProperty<Integer> setDepth(Vertex v, int value) {
      return v.property(TextAnnotation.Properties.depth, value);
    }

    private int getDepth(Vertex v) {
      return getIntValue(v, TextAnnotation.Properties.depth);
    }

    // private void insertUsingOffset0() {
    // // insert after the deepest textannotation that starts at the startingTextSegment, and doesn't end before endingTextSegment
    // // then increase depth of the annotations that are children of the new textannotation
    // GraphTraversal<Vertex, Vertex> tail = storage.getVertexTraversal(startingTextSegment)//
    // .in(EdgeLabels.FIRST_TEXT_SEGMENT)//
    // .hasLabel(VertexLabels.TEXTANNOTATION)//
    // .order().by(TextAnnotation.Properties.depth, Order.incr)//
    // .tail(2L);
    // if (tail.hasNext()) {
    // Vertex deepestTextAnnotationVertex = tail.next();
    // checkVertexLabel(deepestTextAnnotationVertex, VertexLabels.TEXTANNOTATION);
    // int depth = getDepth(deepestTextAnnotationVertex);
    // setDepth(newTextAnnotationVertex, depth + 1);
    // deepestTextAnnotationVertex.addEdge(EdgeLabels.NEXT, newTextAnnotationVertex);
    // GraphTraversal<Vertex, Edge> nextTraversal = storage.getVertexTraversal(deepestTextAnnotationVertex.id())//
    // .outE(EdgeLabels.NEXT);
    // if (nextTraversal.hasNext()) {
    // Edge next = nextTraversal.next();
    // Vertex nextTextAnnotation = next.inVertex();
    // newTextAnnotationVertex.addEdge(EdgeLabels.NEXT, nextTextAnnotation);
    // next.remove();
    // }
    // // Log.info("deepestTextAnnotationVertex={}", visualizeVertex(deepestTextAnnotationVertex));
    // }
    // // Log.info("newTextAnnotationVertex={}", visualizeVertex(newTextAnnotationVertex));
    // // Log.info("startingTextSegment:{}", visualizeVertex(startingTextSegment));
    // }

    boolean rangeStartsInThisTextSegment(Traverser<Vertex> t) {
      incTextSize(t);
      return textSize >= rangeStart;
    }

    void processFirstTextSegmentInRange(Traverser<Vertex> t) {
      Log.debug("processFirstTextSegmentInRange()");
      Vertex textSegmentVertex = t.get();
      checkVertexLabel(textSegmentVertex, VertexLabels.TEXTSEGMENT);

      // if needed, split up the textsegment, preserving the TextAnnotation links
      int tailLength = Math.min(textSize, textSize - rangeStart + 1);
      Log.debug("textSize = {}, tailLength = {}", textSize, tailLength);

      // link the new TextAnnotation to the tail if detaching was necessary, to the firstTextSegment otherwise
      this.startingTextSegment = detachTail(textSegmentVertex, tailLength);
      newTextAnnotationVertex.addEdge(EdgeLabels.FIRST_TEXT_SEGMENT, this.startingTextSegment);
    }

    boolean rangeEndsInThisTextSegment(Traverser<Vertex> t) {
      incTextSize(t);
      Log.debug("textSize:{},rangeEnd:{}", textSize, rangeEnd);
      return textSize >= rangeEnd;
    }

    void processLastTextSegmentInRange(Traverser<Vertex> t) {
      Log.debug("processLastTextSegmentInRange");
      Vertex textSegmentVertex = t.get();
      checkVertexLabel(textSegmentVertex, VertexLabels.TEXTSEGMENT);

      // if needed, split up the textsegment, preserving the TextAnnotation links
      int tailLength = textSize - rangeEnd;
      Log.debug("textSize = {}, tailLength = {}", textSize, tailLength);

      // link the new TextAnnotation to the head if detaching was necessary, to the lastTextSegment otherwise
      this.endingTextSegment = detachHead(textSegmentVertex, tailLength);
      newTextAnnotationVertex.addEdge(EdgeLabels.LAST_TEXT_SEGMENT, this.endingTextSegment);
    }

    private void incTextSize(Traverser<Vertex> t) {
      Vertex textSegmentVertex = t.get();
      checkVertexLabel(textSegmentVertex, VertexLabels.TEXTSEGMENT);
      String text = getStringValue(textSegmentVertex, TextSegment.Properties.text);
      // Log.debug("text=\"{}\"", text);
      textSize += text.length();
      // lastTextSegmentVertex = textSegmentVertex;
    }

    private Vertex detachTail(Vertex textSegment, int tailLength) {
      Preconditions.checkArgument(textSegment.label().equals(VertexLabels.TEXTSEGMENT));
      String text = getStringValue(textSegment, TextSegment.Properties.text);
      int length = text.length();
      textSize = textSize - length;
      int headLength = length - tailLength;
      String headText = text.substring(0, headLength);
      String tailText = text.substring(headLength);
      Log.debug("detachTail(): head = [{}], tail = [{}]", headText, tailText);
      if (headLength == 0) {
        // no detachment necessary?
        return textSegment;

      } else {
        textSegment.property(TextSegment.Properties.text, headText);
        Vertex tailTextSegment = newTextSegmentVertex(tailText);
        insertNewAfterCurrent(tailTextSegment, textSegment);
        // move LAST_TEXT_SEGMENT edges to tailTextSegment
        StreamUtil.stream(textSegment.edges(Direction.IN, EdgeLabels.LAST_TEXT_SEGMENT))//
            .forEach(e -> moveEdge(e, EdgeLabels.LAST_TEXT_SEGMENT, tailTextSegment));
        reindexNeeded = true;
        return tailTextSegment;
      }
    }

    private void insertNewAfterCurrent(Vertex newVertex, Vertex currentVertex) {
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
      Log.debug("detachHead(): head = [{}], tail = [{}]", headText, tailText);
      if (tailLength == 0) {
        // no detachment necessary
        return textSegment;

      } else {
        textSegment.property(TextSegment.Properties.text, tailText);
        Vertex headTextSegment = newTextSegmentVertex(headText);
        // move FIRST_TEXT_SEGMENT edges to tailTextSegment
        insertNewBeforeCurrent(headTextSegment, textSegment);
        StreamUtil.stream(textSegment.edges(Direction.IN, EdgeLabels.FIRST_TEXT_SEGMENT))//
            .forEach(e -> moveEdge(e, EdgeLabels.FIRST_TEXT_SEGMENT, headTextSegment));
        reindexNeeded = true;
        return headTextSegment;
      }
    }

    private void moveEdge(Edge e, String firstTextSegment, Vertex headTextSegment) {
      Vertex textAnnotationVertex = e.outVertex();
      textAnnotationVertex.addEdge(firstTextSegment, headTextSegment);
      e.remove();
    }

    private void insertNewBeforeCurrent(Vertex newVertex, Vertex currentVertex) {
      Iterator<Edge> edges = currentVertex.edges(Direction.IN, EdgeLabels.NEXT);
      if (edges.hasNext()) {
        Edge prevEdge = edges.next();
        Vertex prevVertex = prevEdge.outVertex();
        prevEdge.remove();
        prevVertex.addEdge(EdgeLabels.NEXT, newVertex);
      }
      newVertex.addEdge(EdgeLabels.NEXT, currentVertex);
    }

    private String visualizeVertex(Vertex v) {
      StringBuilder visualization = new StringBuilder();
      visualization.append("\n").append(vertexRepresentation(v));
      StreamUtil.stream(v.edges(Direction.IN)).forEach(e -> visualization.append("\n<-[:").append(e.label()).append("]-").append(vertexRepresentation(e.outVertex())));
      StreamUtil.stream(v.edges(Direction.OUT)).forEach(e -> visualization.append("\n-[:").append(e.label()).append("]->").append(vertexRepresentation(e.inVertex())));
      return visualization.toString();
    }

    private String vertexRepresentation(Vertex v) {
      String props = StreamUtil.stream(v.properties())//
          .map(this::propertyRepresentation)//
          .collect(joining());
      return "(:" + v.label() + "{id:" + v.id() + props + "})";
    }

    private String propertyRepresentation(VertexProperty<Object> vp) {
      return ", " + vp.key() + ":\"" + vp.value().toString().replace("\n", "\\n") + "\"";
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

  private static Vertex lastTextSegment(Vertex parentVertex) {
    return parentVertex.vertices(Direction.OUT, EdgeLabels.LAST_TEXT_SEGMENT).next();
  }

  private static Vertex firstTextSegment(Vertex parentVertex) {
    return parentVertex.vertices(Direction.OUT, EdgeLabels.FIRST_TEXT_SEGMENT).next();
  }

  private Vertex getTextAnnotationVertex(TextAnnotation textAnnotation) {
    Object id = textAnnotation.getId();
    return storage.getVertexTraversal(id).next();
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

  private List<TextAnnotation> getTextAnnotationsToOpen(Vertex textSegment) {
    return getTextAnnotations(textSegment, EdgeLabels.FIRST_TEXT_SEGMENT);
  }

  private List<TextAnnotation> getTextAnnotationsToClose(Vertex textSegment) {
    return Lists.reverse(getTextAnnotations(textSegment, EdgeLabels.LAST_TEXT_SEGMENT));
  }

  private static final Comparator<TextAnnotation> BY_INCREASING_DEPTH = (e1, e2) -> e1.getDepth().compareTo(e2.getDepth());

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

  private void reindex(UUID resourceUUID) {
    reindexTextSegments(resourceUUID);
    reindexTextAnnotations(resourceUUID);
  }

  private void reindexTextSegments(UUID resourceUUID) {
    AtomicInteger counter = new AtomicInteger(1);
    getTextSegmentVertexStream(resourceUUID)//
        .forEach(v -> v.property(TextSegment.Properties.index, counter.getAndIncrement()));
  }

  private void reindexTextAnnotations(UUID resourceUUID) {
    AtomicInteger counter = new AtomicInteger(1);
    getTextAnnotationVertexStream(resourceUUID)//
        .forEach(v -> v.property(TextAnnotation.Properties.index, counter.getAndIncrement()));
  }

}
