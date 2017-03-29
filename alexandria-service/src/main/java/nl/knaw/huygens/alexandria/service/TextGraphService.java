package nl.knaw.huygens.alexandria.service;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * #%L
 * alexandria-service
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

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

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
  private static final Logger LOG = LoggerFactory.getLogger(TextGraphService.class);
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

  public Stream<TextGraphSegment> getTextGraphSegmentStream(UUID resourceUUID, List<List<String>> orderedLayerDefinitions) {
    return getTextSegmentVertexStream(resourceUUID)//
        .map(vertex -> toTextGraphSegment(vertex, orderedLayerDefinitions));
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
          .next();// because there can be only one

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
            // LOG.debug("v={}", v);
            // LOG.debug("updatedVertices={}", updatedVertices);
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
          LOG.error("There seems to be something wrong with the graph.");
          goOn = false;
        }
      }
    }
  }

  public void updateTextAnnotationLink(TextRangeAnnotationVF vf, TextRangeAnnotation textRangeAnnotation, UUID resourceUUID) {
    // LOG.debug("textRangeAnnotation={}", textRangeAnnotation);
    // if the TextRangeAnnotationVF is already linked to a TextAnnotation, remove that TextAnnotation
    FramedGraphTraversal<TextRangeAnnotationVF, Vertex> traversal = vf.out(nl.knaw.huygens.alexandria.storage.frames.TextRangeAnnotationVF.EdgeLabels.HAS_TEXTANNOTATION);
    if (traversal.hasNext()) {
      traversal.next().remove();
    }

    Vertex newTextAnnotationVertex = createNewTextAnnotation(vf, textRangeAnnotation);
    // LOG.debug("textRangeAnnotation={}", textRangeAnnotation);

    TextAnnotationInsertionContext context = new TextAnnotationInsertionContext(newTextAnnotationVertex, textRangeAnnotation);
    Vertex parentTextAnnotationVertex = getVertexTraversalFromResource(resourceUUID)//
        .out(EdgeLabels.HAS_TEXTGRAPH)//
        .out(EdgeLabels.FIRST_ANNOTATION)//

        // find the TextAnnotation with the xml:id from annotation.position.xmlid
        .until(__.has(TextAnnotation.Properties.xmlid, textRangeAnnotation.getAbsolutePosition().getXmlId()))//
        .repeat(__.out(EdgeLabels.NEXT))//
        .next();
    if (context.annotationIsMilestone) {
      handleMilestoneAnnotation(newTextAnnotationVertex, context, parentTextAnnotationVertex);
    } else {
      handleRegularAnnotation(resourceUUID, context, parentTextAnnotationVertex);
    }
    if (context.reindexNeeded) {
      reindex(resourceUUID);
    }
    context.insertNewTextAnnotationVertex();
    reindex(resourceUUID);
    if (LOG.isDebugEnabled()) {
      LOG.debug("newTextAnnotationVertex={}", visualizeVertex(newTextAnnotationVertex));
    }
  }

  private void handleRegularAnnotation(UUID resourceUUID, TextAnnotationInsertionContext context, Vertex parentTextAnnotationVertex) {
    LOG.debug("handleMilestoneAnnotation");
    List<Vertex> list = storage.getVertexTraversal(parentTextAnnotationVertex)//
        .out(EdgeLabels.FIRST_TEXT_SEGMENT)//
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
      LOG.error("listsize should be 1, is {}", list.size());
    }
  }

  private void handleMilestoneAnnotation(Vertex newTextAnnotationVertex, TextAnnotationInsertionContext context, Vertex parentTextAnnotationVertex) {
    LOG.debug("handleMilestoneAnnotation");
    // TODO: refactor this mess!

    int startOffset = 0;
    int endOffset = 0;
    String text = "";
    Vertex textSegment = parentTextAnnotationVertex.vertices(Direction.OUT, EdgeLabels.FIRST_TEXT_SEGMENT).next();
    boolean parentTextAnnotationIsMilestone = parentTextAnnotationIsMilestone(parentTextAnnotationVertex, textSegment);

    if (parentTextAnnotationIsMilestone) {
      context.linkTextAnnotationToTextSegment(newTextAnnotationVertex, textSegment);

    } else {
      Vertex emptyTextSegment = newTextSegmentVertex("");
      context.linkTextAnnotationToTextSegment(newTextAnnotationVertex, emptyTextSegment);

      // find the textsegment where the textrange from annotation.position starts
      boolean goOn = true;
      while (goOn) {
        text = getStringValue(textSegment, TextSegment.Properties.text);
        int segmentSize = text.length();
        goOn = startOffset + segmentSize < context.rangeStart;
        endOffset += segmentSize;
        if (goOn) {
          Iterator<Vertex> nextTextSegments = textSegment.vertices(Direction.OUT, EdgeLabels.NEXT);
          if (nextTextSegments.hasNext()) {
            startOffset += segmentSize;
            textSegment = nextTextSegments.next();
          } else {
            goOn = false;
          }
        }
      }
      if (startOffset == context.rangeStart) {
        context.insertNewBeforeCurrent(emptyTextSegment, textSegment);
        StreamUtil.stream(textSegment.edges(Direction.IN, EdgeLabels.FIRST_TEXT_SEGMENT))//
            .forEach(e -> context.moveEdge(e, EdgeLabels.FIRST_TEXT_SEGMENT, emptyTextSegment));

      } else if (endOffset == context.rangeEnd) {
        context.insertNewAfterCurrent(emptyTextSegment, textSegment);
        StreamUtil.stream(textSegment.edges(Direction.IN, EdgeLabels.LAST_TEXT_SEGMENT))//
            .forEach(e -> context.moveEdge(e, EdgeLabels.LAST_TEXT_SEGMENT, emptyTextSegment));

      } else {
        // split up textSegment?
        int headLength = context.rangeStart - startOffset - 1;
        String headText = text.substring(0, headLength);
        String tailText = text.substring(headLength);
        textSegment.property(TextSegment.Properties.text, headText);
        Vertex newTail = newTextSegmentVertex(tailText);
        StreamUtil.stream(textSegment.edges(Direction.IN, EdgeLabels.LAST_TEXT_SEGMENT))//
            .forEach(e -> context.moveEdge(e, EdgeLabels.LAST_TEXT_SEGMENT, newTail));
        context.insertNewAfterCurrent(newTail, textSegment);
        context.insertNewAfterCurrent(emptyTextSegment, textSegment);
      }
    }

    context.reindexNeeded = true;
  }

  private boolean parentTextAnnotationIsMilestone(Vertex parentTextAnnotationVertex, Vertex textSegment) {
    Vertex lastTextSegment = parentTextAnnotationVertex.vertices(Direction.OUT, EdgeLabels.LAST_TEXT_SEGMENT).next();
    String firstText = getStringValue(textSegment, TextSegment.Properties.text);
    return firstText.isEmpty() && textSegment.equals(lastTextSegment);
  }

  private Vertex createNewTextAnnotation(TextRangeAnnotationVF textRangeAnnotationVF, TextRangeAnnotation textRangeAnnotation) {
    // create new TextAnnotation
    Map<String, String> attributes = ImmutableMap.<String, String> builder()//
        .putAll(textRangeAnnotation.getAttributes())//
        .put(TextRangeAnnotation.RESPONSIBILITY_ATTRIBUTE, "#" + textRangeAnnotation.getAnnotator())//
        .build();
    TextAnnotation newTextAnnotation = new TextAnnotation(textRangeAnnotation.getName(), attributes, 1000); // 1000 is temporary depth, adjust depth once place in textannotationlist has been
    // determined
    Vertex newTextAnnotationVertex = toVertex(newTextAnnotation);

    // link TextAnnotation to TextRangeAnnotation
    textRangeAnnotationVF.vertex().addEdge(TextRangeAnnotationVF.EdgeLabels.HAS_TEXTANNOTATION, newTextAnnotationVertex);
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
      // LOG.debug("textRangeAnnotation={}", textRangeAnnotation);
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
      // LOG.debug("range = [{},{}]", rangeStart, rangeEnd);
    }

    private void linkTextAnnotationToTextSegment(Vertex newTextAnnotationVertex, Vertex emptyTextSegment) {
      this.startingTextSegment = emptyTextSegment;
      this.endingTextSegment = emptyTextSegment;
      newTextAnnotationVertex.addEdge(EdgeLabels.FIRST_TEXT_SEGMENT, this.startingTextSegment);
      newTextAnnotationVertex.addEdge(EdgeLabels.LAST_TEXT_SEGMENT, this.endingTextSegment);
    }

    void insertNewTextAnnotationVertex() {
      // LOG.info("startingTextSegment:{}", visualizeVertex(startingTextSegment));
      if (useOffset) {
        insertUsingOffset();

      } else {
        // then straight after the parent annotation
        insertAfterParent();
      }
    }

    private void insertAfterParent() {
      // LOG.debug("startingTextSegment={}", visualizeVertex(startingTextSegment));
      Vertex parentVertex = storage.getVertexTraversal(startingTextSegment)//
          .in(EdgeLabels.FIRST_TEXT_SEGMENT)//
          .hasLabel(VertexLabels.TEXTANNOTATION)//
          .has(TextAnnotation.Properties.xmlid, parentXmlId)//
          .next();
      // LOG.debug("parentVertex={}", visualizeVertex(parentVertex));
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
      // LOG.debug("startIndex,endIndex=({},{})", startingTextSegmentIndex, endingTextSegmentIndex);
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
    // // LOG.info("deepestTextAnnotationVertex={}", visualizeVertex(deepestTextAnnotationVertex));
    // }
    // // LOG.info("newTextAnnotationVertex={}", visualizeVertex(newTextAnnotationVertex));
    // // LOG.info("startingTextSegment:{}", visualizeVertex(startingTextSegment));
    // }

    boolean rangeStartsInThisTextSegment(Traverser<Vertex> t) {
      incTextSize(t);
      return textSize >= rangeStart;
    }

    void processFirstTextSegmentInRange(Traverser<Vertex> t) {
      LOG.debug("processFirstTextSegmentInRange()");
      Vertex textSegmentVertex = t.get();
      checkVertexLabel(textSegmentVertex, VertexLabels.TEXTSEGMENT);

      // if needed, split up the textsegment, preserving the TextAnnotation links
      int tailLength = Math.min(textSize, textSize - rangeStart + 1);
      LOG.debug("textSize = {}, tailLength = {}", textSize, tailLength);

      // link the new TextAnnotation to the tail if detaching was necessary, to the firstTextSegment otherwise
      this.startingTextSegment = detachTail(textSegmentVertex, tailLength);
      newTextAnnotationVertex.addEdge(EdgeLabels.FIRST_TEXT_SEGMENT, this.startingTextSegment);
    }

    boolean rangeEndsInThisTextSegment(Traverser<Vertex> t) {
      incTextSize(t);
      LOG.debug("textSize:{},rangeEnd:{}", textSize, rangeEnd);
      return textSize >= rangeEnd;
    }

    void processLastTextSegmentInRange(Traverser<Vertex> t) {
      LOG.debug("processLastTextSegmentInRange");
      Vertex textSegmentVertex = t.get();
      checkVertexLabel(textSegmentVertex, VertexLabels.TEXTSEGMENT);

      // if needed, split up the textsegment, preserving the TextAnnotation links
      int tailLength = textSize - rangeEnd;
      LOG.debug("textSize = {}, tailLength = {}", textSize, tailLength);

      // link the new TextAnnotation to the head if detaching was necessary, to the lastTextSegment otherwise
      this.endingTextSegment = detachHead(textSegmentVertex, tailLength);
      newTextAnnotationVertex.addEdge(EdgeLabels.LAST_TEXT_SEGMENT, this.endingTextSegment);
    }

    private void incTextSize(Traverser<Vertex> t) {
      Vertex textSegmentVertex = t.get();
      checkVertexLabel(textSegmentVertex, VertexLabels.TEXTSEGMENT);
      String text = getStringValue(textSegmentVertex, TextSegment.Properties.text);
      // LOG.debug("text=\"{}\"", text);
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
      LOG.debug("detachTail(): head = [{}], tail = [{}]", headText, tailText);
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
      int headLength = length - tailLength;
      String headText = text.substring(0, headLength);
      String tailText = text.substring(headLength);
      LOG.debug("detachHead(): head = [{}], tail = [{}]", headText, tailText);
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

  private TextGraphSegment toTextGraphSegment(Vertex textSegment, List<List<String>> orderedLayerDefinitions) {
    TextGraphSegment textGraphSegment = new TextGraphSegment();
    if (textSegment.keys().contains(TextSegment.Properties.text)) {
      textGraphSegment.setTextSegment(textSegment.value(TextSegment.Properties.text));
    }
    List<TextAnnotation> textAnnotationsToOpen = getTextAnnotationsToOpen(textSegment, orderedLayerDefinitions);
    List<TextAnnotation> textAnnotationsToClose = getTextAnnotationsToClose(textSegment, orderedLayerDefinitions);

    boolean isMilestone = StringUtils.isEmpty(textGraphSegment.getTextSegment())//
        && !textAnnotationsToOpen.isEmpty()//
        && !textAnnotationsToClose.isEmpty();
    if (isMilestone) {
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

  private List<TextAnnotation> getTextAnnotationsToOpen(Vertex textSegment, List<List<String>> orderedLayerDefinitions) {
    return getTextAnnotations(textSegment, EdgeLabels.FIRST_TEXT_SEGMENT, orderedLayerDefinitions);
  }

  private List<TextAnnotation> getTextAnnotationsToClose(Vertex textSegment, List<List<String>> orderedLayerDefinitions) {
    return Lists.reverse(getTextAnnotations(textSegment, EdgeLabels.LAST_TEXT_SEGMENT, orderedLayerDefinitions));
  }

  private static final Comparator<TextAnnotation> BY_INCREASING_DEPTH = Comparator.comparing(TextAnnotation::getDepth);

  private List<TextAnnotation> getTextAnnotations(Vertex textSegment, String edgeLabel, List<List<String>> orderedLayerDefinitions) {
    // LOG.info("orderedLayerDefinitions:'{}'",orderedLayerDefinitions);
    // LOG.info("textsegment:'{}'",(String) textSegment.value("text"));
    List<Vertex> textAnnotationVertexList = StreamUtil.stream(textSegment.vertices(Direction.IN, edgeLabel))//
        .filter(v -> v.label().equals(VertexLabels.TEXTANNOTATION)).collect(toList());
    // LOG.info("textAnnotationVertexList.size={}",textAnnotationVertexList.size());
    List<List<Vertex>> vertexListPerLayer = new ArrayList<>();
    AtomicInteger relevantVertexCount = new AtomicInteger(0);
    List<Vertex> otherVertexList = Lists.newArrayList(textAnnotationVertexList);

    // process tags defined in orderedLayerDefinitions
    orderedLayerDefinitions.forEach(layerTags -> {
      List<Vertex> vertexList = textAnnotationVertexList.stream()//
          .filter(v -> layerTags.contains(v.value(TextAnnotation.Properties.name)))//
          .collect(toList());
      vertexListPerLayer.add(vertexList);
      relevantVertexCount.set(relevantVertexCount.get() + vertexList.size());
      otherVertexList.removeAll(vertexList);
    });
    // put all other tags in separate layers, grouped by depth
    // LOG.info("otherVertexList.size={}",otherVertexList.size());
    vertexListPerLayer.addAll(groupByDecreasingDepth(otherVertexList));
    relevantVertexCount.set(relevantVertexCount.get() + otherVertexList.size());

    boolean useLayerOrder = relevantVertexCount.get() > 1;
    Map<Vertex, Integer> overriddenDepth = new HashMap<>();
    if (useLayerOrder) {
      createPairs(vertexListPerLayer).stream()//
          .filter(this::hasSameTextRange)//
          .forEach(pair -> {
            Vertex leftVertex = pair.getLeft();
            Vertex rightVertex = pair.getRight();
            Integer leftDepth = getDepth(leftVertex, overriddenDepth);
            Integer rightDepth = getDepth(rightVertex, overriddenDepth);
            boolean swapDepths = leftDepth < rightDepth;
            if (swapDepths) {
              overriddenDepth.put(leftVertex, rightDepth);
              overriddenDepth.put(rightVertex, leftDepth);
            }
          });
    }
    Function<Vertex, TextAnnotation> toTextAnnotationWithOverriddenDepth = vertex -> {
      TextAnnotation textAnnotation = toTextAnnotation(vertex);
      if (overriddenDepth.containsKey(vertex)) {
        textAnnotation.setDepth(overriddenDepth.get(vertex));
      }
      return textAnnotation;
    };
    return textAnnotationVertexList.stream()//
        .map(toTextAnnotationWithOverriddenDepth)//
        .sorted(BY_INCREASING_DEPTH)//
        .collect(toList());
  }

  private Integer getDepth(Vertex leftVertex, Map<Vertex, Integer> overriddenDepth) {
    return overriddenDepth.containsKey(leftVertex)//
        ? overriddenDepth.get(leftVertex)//
        : (Integer) leftVertex.value(TextAnnotation.Properties.depth);
  }

  List<List<Vertex>> groupByDecreasingDepth(List<Vertex> vertexList) {
    Map<Integer, List<Vertex>> groupedByDepth = vertexList.stream()//
        .collect(groupingBy(v -> (Integer) v.value(TextAnnotation.Properties.depth)));
    return groupedByDepth.keySet().stream()//
        .sorted((d0, d1) -> d1.compareTo(d0))//
        .map(d -> groupedByDepth.get(d))//
        .collect(toList());
  }

  List<Pair<Vertex, Vertex>> createPairs(List<List<Vertex>> vertexListPerLayer) {
    List<Pair<Vertex, Vertex>> pairList = Lists.newArrayList();
    for (int i = 0; i < vertexListPerLayer.size() - 1; i++) {
      List<Vertex> vertexLayer1 = vertexListPerLayer.get(i);
      for (int j = i + 1; j < vertexListPerLayer.size(); j++) {
        List<Vertex> vertexLayer2 = vertexListPerLayer.get(j);
        pairList.addAll(createPairs(vertexLayer1, vertexLayer2));
      }
    }
    return pairList;
  }

  private List<Pair<Vertex, Vertex>> createPairs(List<Vertex> vertexLayer1, List<Vertex> vertexLayer2) {
    List<Pair<Vertex, Vertex>> list = new ArrayList<>();
    vertexLayer1.forEach(v1 -> {
      vertexLayer2.forEach(v2 -> {
        list.add(Pair.of(v1, v2));
      });
    });
    return list;
  }

  Boolean hasSameTextRange(Pair<Vertex, Vertex> vertexPair) {
    Vertex left = vertexPair.getLeft();
    Vertex leftFirstTextSegment = left.vertices(Direction.OUT, EdgeLabels.FIRST_TEXT_SEGMENT).next();
    Vertex leftLastTextSegment = left.vertices(Direction.OUT, EdgeLabels.LAST_TEXT_SEGMENT).next();

    Vertex right = vertexPair.getRight();
    Vertex rightFirstTextSegment = right.vertices(Direction.OUT, EdgeLabels.FIRST_TEXT_SEGMENT).next();
    Vertex rightLastTextSegment = right.vertices(Direction.OUT, EdgeLabels.LAST_TEXT_SEGMENT).next();

    return leftFirstTextSegment.equals(rightFirstTextSegment) //
        && leftLastTextSegment.equals(rightLastTextSegment);
  }

  private TextAnnotation toTextAnnotation(Vertex vertex) {
    Map<String, String> attributes = getAttributeMap(vertex);
    TextAnnotation textAnnotation = new TextAnnotation(//
        vertex.value(TextAnnotation.Properties.name), //
        attributes, //
        vertex.value(TextAnnotation.Properties.depth)//
    );
    Iterator<Vertex> textRangeAnnotations = vertex.vertices(Direction.IN, TextRangeAnnotationVF.EdgeLabels.HAS_TEXTANNOTATION);
    Object id = textRangeAnnotations.hasNext()//
        ? textRangeAnnotations.next().properties(Storage.IDENTIFIER_PROPERTY).next().value()//
        : "?" /* UUID.randomUUID() */;
    textAnnotation.setId(id);
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
