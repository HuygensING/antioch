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
import java.util.stream.StreamSupport;

import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import com.google.common.collect.Maps;

import nl.knaw.huygens.Log;
import nl.knaw.huygens.alexandria.storage.EdgeLabels;
import nl.knaw.huygens.alexandria.storage.Storage;
import nl.knaw.huygens.alexandria.storage.VertexLabels;
import nl.knaw.huygens.alexandria.textgraph.ParseResult;
import nl.knaw.huygens.alexandria.textgraph.TextAnnotation;
import nl.knaw.huygens.alexandria.textgraph.TextGraphSegment;
import nl.knaw.huygens.alexandria.textgraph.XmlAnnotation;

public class TextGraphService {
  private Storage storage;

  public TextGraphService(Storage storage) {
    this.storage = storage;
  }

  public void storeTextGraph(UUID resourceId, ParseResult result) {
    storage.runInTransaction(() -> {
      Vertex resource = storage.getResourceVertexTraversal()//
          .has(Storage.IDENTIFIER_PROPERTY, resourceId.toString())//
          .next();
      Vertex text = storage.addVertex(T.label, VertexLabels.TEXTGRAPH);
      resource.addEdge(EdgeLabels.HAS_TEXTGRAPH, text);
      resource.property("hasText", true);
      List<Vertex> textSegments = storeTextSegments(result.getTextSegments(), text);
      storeTextAnnotations(result.getXmlAnnotations(), text, textSegments);
    });
  }

  public Stream<TextGraphSegment> getTextGraphSegmentStream(UUID resourceId) {
    Iterator<Vertex> textSegmentIterator = new Iterator<Vertex>() {
      Vertex textSegment = storage.getResourceVertexTraversal()//
          .has(Storage.IDENTIFIER_PROPERTY, resourceId.toString())//
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
    Iterable<Vertex> iterable = () -> textSegmentIterator;
    return StreamSupport.stream(iterable.spliterator(), false)//
        .map(this::toTextGraphSegment);
  }

  public Stream<TextAnnotation> getTextAnnotationStream(UUID resourceId) {
    Iterator<Vertex> textAnnotationIterator = new Iterator<Vertex>() {
      Vertex textAnnotationVertex = storage.getResourceVertexTraversal()//
          .has(Storage.IDENTIFIER_PROPERTY, resourceId.toString())//
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
    Iterable<Vertex> iterable = () -> textAnnotationIterator;
    return StreamSupport.stream(iterable.spliterator(), false)//
        .map(this::toTextAnnotation);
  }

  public void updateTextAnnotation(TextAnnotation textAnnotation) {
    Vertex vertex = getTextAnnotationVertex(textAnnotation);
    update(vertex, textAnnotation);
  }

  public void wrapContentInChildTextAnnotation(TextAnnotation existingTextAnnotation, TextAnnotation newTextAnnotation) {
    Vertex parentVertex = getTextAnnotationVertex(existingTextAnnotation);
    Log.debug("wrapContentInChildTextAnnotation: parent={}", parentVertex);
    Iterator<Edge> parentOutEdges = parentVertex.edges(Direction.OUT, EdgeLabels.FIRST_TEXT_SEGMENT, EdgeLabels.LAST_TEXT_SEGMENT, EdgeLabels.NEXT);
    Vertex childVertex = toVertex(newTextAnnotation);
    Log.debug("wrapContentInChildTextAnnotation: child={}", childVertex);

    // copy FIRST_TEXT_SEGMENT, LAST_TEXT_SEGMENT, NEXT edges from parentVertex to childVertex
    while (parentOutEdges.hasNext()) {
      Edge outEdge = parentOutEdges.next();
      Log.debug("wrapContentInChildTextAnnotation: outEdge={}", outEdge);
      childVertex.addEdge(outEdge.label(), outEdge.inVertex());
    }
    // remove existing NEXT edge for parentVertex, replace with NEXT edge pointing to childVertex
    Iterator<Edge> parentNextEdgeIterator = parentVertex.edges(Direction.OUT, EdgeLabels.NEXT);
    if (parentNextEdgeIterator.hasNext()) {
      Edge nextEdge = parentNextEdgeIterator.next();
      Log.debug("wrapContentInChildTextAnnotation: nextEdge={}", nextEdge);
      nextEdge.remove();
      Edge newNextEdge = parentVertex.addEdge(EdgeLabels.NEXT, childVertex);
      Log.debug("wrapContentInChildTextAnnotation: newNextEdge={}", newNextEdge);
    }
  }

  // private methods //

  private Vertex getTextAnnotationVertex(TextAnnotation textAnnotation) {
    Object id = textAnnotation.getId();
    Vertex vertex = storage.getVertexTraversal(id).next();
    return vertex;
  }

  private List<Vertex> storeTextSegments(List<String> textSegments, Vertex text) {
    List<Vertex> textSegmentVertices = new ArrayList<>();
    Vertex previous = null;
    for (String s : textSegments) {
      Vertex v = storage.addVertex(T.label, VertexLabels.TEXTSEGMENT, TextSegment.Properties.text, s);
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

  private Vertex toVertex(TextAnnotation textAnnotation) {
    Vertex v = storage.addVertex(T.label, VertexLabels.TEXTANNOTATION);
    update(v, textAnnotation);
    return v;
  }

  private void update(Vertex v, TextAnnotation textAnnotation) {
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
  }

  private TextGraphSegment toTextGraphSegment(Vertex textSegment) {
    TextGraphSegment textGraphSegment = new TextGraphSegment();
    if (textSegment.keys().contains(TextSegment.Properties.text)) {
      textGraphSegment.setTextSegment(textSegment.value(TextSegment.Properties.text));
    }
    textGraphSegment.setAnnotationsToOpen(getTextAnnotationsToOpen(textSegment));
    textGraphSegment.setAnnotationsToClose(getTextAnnotationsToClose(textSegment));
    return textGraphSegment;
  }

  private static final Comparator<TextAnnotation> BY_DECREASING_DEPTH = (e1, e2) -> e1.getDepth().compareTo(e2.getDepth());

  private List<TextAnnotation> getTextAnnotationsToOpen(Vertex textSegment) {
    return getTextAnnotations(textSegment, EdgeLabels.FIRST_TEXT_SEGMENT, BY_DECREASING_DEPTH);
  }

  private static final Comparator<TextAnnotation> BY_INCREASING_DEPTH = (e1, e2) -> e2.getDepth().compareTo(e1.getDepth());

  private List<TextAnnotation> getTextAnnotationsToClose(Vertex textSegment) {
    return getTextAnnotations(textSegment, EdgeLabels.LAST_TEXT_SEGMENT, BY_INCREASING_DEPTH);
  }

  private List<TextAnnotation> getTextAnnotations(Vertex textSegment, String edgeLabel, Comparator<TextAnnotation> comparator) {
    Iterable<Vertex> iterable = () -> textSegment.vertices(Direction.IN, edgeLabel);
    return StreamSupport.stream(iterable.spliterator(), false)//
        .filter(v -> v.label().equals(VertexLabels.TEXTANNOTATION))//
        .map(this::toTextAnnotation)//
        .sorted(comparator)//
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

}
