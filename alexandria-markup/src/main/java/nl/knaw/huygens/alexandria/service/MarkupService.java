package nl.knaw.huygens.alexandria.service;

import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import nl.knaw.huygens.alexandria.api.model.Annotator;
import nl.knaw.huygens.alexandria.api.model.AnnotatorList;
import nl.knaw.huygens.alexandria.api.model.text.TextRangeAnnotation;
import nl.knaw.huygens.alexandria.api.model.text.TextRangeAnnotation.AbsolutePosition;
import nl.knaw.huygens.alexandria.api.model.text.TextRangeAnnotation.Position;
import nl.knaw.huygens.alexandria.api.model.text.TextRangeAnnotationList;
import nl.knaw.huygens.alexandria.api.model.text.view.TextView;
import nl.knaw.huygens.alexandria.api.model.text.view.TextViewDefinition;
import nl.knaw.huygens.alexandria.endpoint.LocationBuilder;
import nl.knaw.huygens.alexandria.exception.NotFoundException;
import nl.knaw.huygens.alexandria.model.AlexandriaResource;
import nl.knaw.huygens.alexandria.storage.Storage;
import nl.knaw.huygens.alexandria.storage.frames.AnnotatorVF;
import nl.knaw.huygens.alexandria.storage.frames.ResourceVF;
import nl.knaw.huygens.alexandria.storage.frames.TextRangeAnnotationVF;
import nl.knaw.huygens.alexandria.storage.frames.TextRangeAnnotationVF.EdgeLabels;
import nl.knaw.huygens.alexandria.textgraph.ParseResult;
import nl.knaw.huygens.alexandria.textgraph.TextAnnotation;
import nl.knaw.huygens.alexandria.textgraph.TextGraphSegment;
import nl.knaw.huygens.alexandria.util.StreamUtil;
import peapod.FramedGraphTraversal;

public class MarkupService extends TinkerPopService implements IMarkupService {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().registerModule(new Jdk8Module());

  private TextGraphService textGraphService;
  private static final TypeReference<Map<String, TextView>> TEXTVIEW_TYPEREF = new TypeReference<Map<String, TextView>>() {
  };
  private static final ObjectReader TEXTVIEW_READER = OBJECT_MAPPER.readerFor(TEXTVIEW_TYPEREF);
  private static final ObjectWriter TEXTVIEW_WRITER = OBJECT_MAPPER.writerFor(TEXTVIEW_TYPEREF);

  private static final TypeReference<Map<String, TextViewDefinition>> TEXTVIEWDEFINITION_TYPEREF = new TypeReference<Map<String, TextViewDefinition>>() {
  };
  private static final ObjectReader TEXTVIEWDEFINITION_READER = OBJECT_MAPPER.readerFor(TEXTVIEWDEFINITION_TYPEREF);
  private static final ObjectWriter TEXTVIEWDEFINITION_WRITER = OBJECT_MAPPER.writerFor(TEXTVIEWDEFINITION_TYPEREF);

  public MarkupService(Storage storage, LocationBuilder locationBuilder) {
    super(storage, locationBuilder);
  }

  @Override
  public void setResourceAnnotator(UUID resourceUUID, Annotator annotator) {
    storage.runInTransaction(() -> {
      // remove existing annotator for this resource with the same annotator code
      storage.getResourceVertexTraversal()//
          .has(Storage.IDENTIFIER_PROPERTY, resourceUUID.toString())//
          .in(EdgeLabels.HAS_RESOURCE)//
          .has("code", annotator.getCode())//
          .toList()//
          .forEach(Vertex::remove);
      AnnotatorVF avf = frameAnnotator(annotator);
      ResourceVF resourceVF = storage.readVF(ResourceVF.class, resourceUUID).get();
      avf.setResource(resourceVF);
      // Log.info("avf.resource={}", avf.getResource().getUuid());
    });

  }

  @Override
  public Optional<Annotator> readResourceAnnotator(UUID uuid, String code) {
    ResourceVF resourcevf = readExistingResourceVF(uuid);
    return resourcevf.getAnnotators().stream()//
        .map(this::deframeAnnotator)//
        .filter(a -> code.equals(a.getCode()))//
        .findAny();
  }

  @Override
  public AnnotatorList readResourceAnnotators(UUID uuid) {
    List<AnnotatorVF> annotatorVFs = storage.runInTransaction(() -> {
      ResourceVF resourceVF = readExistingResourceVF(uuid);
      List<AnnotatorVF> annotatorVFList = Lists.newArrayList();
      do {
        annotatorVFList.addAll(resourceVF.getAnnotators());
        resourceVF = resourceVF.getParentResource();
      } while (resourceVF != null);

      return annotatorVFList;
    });
    AnnotatorList annotators = new AnnotatorList();
    Set<String> codes = Sets.newHashSet();
    annotatorVFs.stream().map(this::deframeAnnotator)//
        .forEach(a -> {
          if (!codes.contains(a.getCode())) {
            codes.add(a.getCode());
            annotators.add(a);
          }
        });
    return annotators;
  }

  @Override
  public void setTextRangeAnnotation(UUID resourceUUID, TextRangeAnnotation annotation) {
    storage.runInTransaction(() -> {
      TextRangeAnnotationVF vf = storage.readVF(TextRangeAnnotationVF.class, annotation.getId())//
          .orElseGet(() -> storage.createVF(TextRangeAnnotationVF.class));
      updateTextRangeAnnotation(vf, annotation);
      textGraphService.updateTextAnnotationLink(vf, annotation, resourceUUID);
      vf.setResource(storage.readVF(ResourceVF.class, resourceUUID).get());
    });
  }

  @Override
  public void deprecateTextRangeAnnotation(UUID annotationUUID, TextRangeAnnotation newTextRangeAnnotation) {
    storage.runInTransaction(() -> {
      TextRangeAnnotationVF oldVF = storage.readVF(TextRangeAnnotationVF.class, annotationUUID)//
          .orElseThrow(NotFoundException::new);
      Integer revision = oldVF.getRevision();
      oldVF.setUuid(annotationUUID.toString() + "." + revision);
      ResourceVF resourceVF = oldVF.getResource();
      oldVF.setResource(null);

      removeTextAnnotationFromChain(oldVF);

      TextRangeAnnotationVF newVF = storage.createVF(TextRangeAnnotationVF.class);

      newTextRangeAnnotation.setRevision(revision + 1);
      updateTextRangeAnnotation(newVF, newTextRangeAnnotation);
      UUID resourceUUID = UUID.fromString(resourceVF.getUuid());
      textGraphService.updateTextAnnotationLink(newVF, newTextRangeAnnotation, resourceUUID);
      newVF.setResource(resourceVF);
      newVF.setDeprecatedAnnotation(oldVF);
    });
  }

  private void removeTextAnnotationFromChain(TextRangeAnnotationVF oldVF) {
    FramedGraphTraversal<TextRangeAnnotationVF, Vertex> traversal = oldVF.out(TextRangeAnnotationVF.EdgeLabels.HAS_TEXTANNOTATION);

    // remove the old textAnnotationVertex without breaking the chain.
    if (traversal.hasNext()) {
      Vertex textAnnotationVertex = traversal.next();
      Vertex leftVertex = null; // in the annotation chain, the vertex to the left of this textAnnotationVertex; there is always a left vertex
      String leftEdgeLabel = null;
      Vertex rightVertex = null; // in the annotation chain, the vertex to the right of this textAnnotationVertex; there might not be a right vertex
      // it might be the first in the chain, so it has an incoming FIRST_ANNOTATION edge
      Iterator<Edge> incomingFirstAnnotationEdgeIterator = textAnnotationVertex.edges(Direction.IN, nl.knaw.huygens.alexandria.storage.EdgeLabels.FIRST_ANNOTATION);
      if (incomingFirstAnnotationEdgeIterator.hasNext()) {
        // in that case, remove the edge, and reconnect the chain.
        Edge incomingEdge = incomingFirstAnnotationEdgeIterator.next();
        leftVertex = incomingEdge.outVertex();
        leftEdgeLabel = nl.knaw.huygens.alexandria.storage.EdgeLabels.FIRST_ANNOTATION;
        incomingEdge.remove();

      } else {
        // otherwise, it's a NEXT edge
        Iterator<Edge> incomingNextEdgeIterator = textAnnotationVertex.edges(Direction.IN, nl.knaw.huygens.alexandria.storage.EdgeLabels.NEXT);
        if (incomingNextEdgeIterator.hasNext()) {
          Edge incomingNextEdge = incomingNextEdgeIterator.next();
          leftVertex = incomingNextEdge.outVertex();
          incomingNextEdge.remove();
        }
        leftEdgeLabel = nl.knaw.huygens.alexandria.storage.EdgeLabels.NEXT;
      }

      Iterator<Edge> outgoingNextEdgeIterator = textAnnotationVertex.edges(Direction.OUT, nl.knaw.huygens.alexandria.storage.EdgeLabels.NEXT);
      if (outgoingNextEdgeIterator.hasNext()) {
        Edge outgoingNextEdge = outgoingNextEdgeIterator.next();
        rightVertex = outgoingNextEdge.inVertex();
        outgoingNextEdge.remove();
      }

      if (rightVertex != null) {
        leftVertex.addEdge(leftEdgeLabel, rightVertex);
      }

      textAnnotationVertex.remove();
    }
  }

  private TextRangeAnnotationList getTextRangeAnnotationList(UUID resourceUUID) {
    TextRangeAnnotationList list = new TextRangeAnnotationList();
    storage.readVF(ResourceVF.class, resourceUUID).ifPresent(resourceVF -> {
      FramedGraphTraversal<ResourceVF, Vertex> traversal = resourceVF.start()//
          .in(TextRangeAnnotationVF.EdgeLabels.HAS_RESOURCE)//
      ;
      StreamUtil.stream(traversal)//
          .map(v -> storage.frameVertex(v, TextRangeAnnotationVF.class))//
          .map(this::deframeTextRangeAnnotation)//
          .forEach(list::add);
    });
    return list;
  }

  @Override
  public Optional<TextRangeAnnotation> readTextRangeAnnotation(UUID resourceUUID, UUID annotationUUID) {
    return storage.runInTransaction(() -> getOptionalTextRangeAnnotation(resourceUUID, annotationUUID));
  }

  @Override
  public Optional<TextRangeAnnotation> readTextRangeAnnotation(UUID resourceUUID, UUID annotationUUID, Integer revision) {
    return storage.runInTransaction(() -> {
      Optional<TextRangeAnnotationVF> versionedAnnotation = storage.readVF(TextRangeAnnotationVF.class, annotationUUID, revision);
      if (versionedAnnotation.isPresent()) {
        return versionedAnnotation.map(this::deframeTextRangeAnnotation);

      } else {
        Optional<TextRangeAnnotationVF> currentAnnotation = storage.readVF(TextRangeAnnotationVF.class, annotationUUID);
        if (currentAnnotation.isPresent() && currentAnnotation.get().getRevision().equals(revision)) {
          return currentAnnotation.map(this::deframeTextRangeAnnotation);
        } else {
          return Optional.empty();
        }
      }
    });
  }

  private Optional<TextRangeAnnotation> getOptionalTextRangeAnnotation(UUID resourceUUID, UUID annotationUUID) {
    return storage.readVF(TextRangeAnnotationVF.class, annotationUUID).map(this::deframeTextRangeAnnotation);
  }

  @Override
  public boolean nonNestingOverlapWithExistingTextRangeAnnotationForResource(TextRangeAnnotation annotation, UUID resourceUUID) {
    return storage.runInTransaction(() -> {
      AtomicBoolean overlaps = new AtomicBoolean(false);
      storage.readVF(ResourceVF.class, resourceUUID).ifPresent(resourceVF -> {
        FramedGraphTraversal<ResourceVF, Vertex> traversal = resourceVF.start()//
            .in(TextRangeAnnotationVF.EdgeLabels.HAS_RESOURCE)//
            .has("name", annotation.getName())//
            .has("annotatorCode", annotation.getAnnotator())//
        ;
        AbsolutePosition absolutePosition = annotation.getAbsolutePosition();
        String uuid1 = annotation.getId().toString();
        String xmlId1 = absolutePosition.getXmlId();
        Integer start1 = absolutePosition.getOffset();
        Integer end1 = start1 + absolutePosition.getLength();
        Predicate<Vertex> nonNestingOverlapWithAnnotation = t -> {
          String xmlId2 = (String) t.property("absoluteXmlId").value();
          Integer start2 = (Integer) t.property("absoluteOffset").value();
          Integer end2 = start2 + (Integer) t.property("absoluteLength").value();
          return xmlId1.equals(xmlId2)//
              && (start1 < end2 && start2 < end1) // annotation overlaps with existing annotation t
              && !((start1 <= start2 && start2 <= end1 && end2 <= end1) && !(start1 == start2 && end1 == end2)) // existing annotation t nested in annotation
              && !((start2 <= start1 && start1 <= end2 && end1 <= end2) && !(start1 == start2 && end1 == end2)) // annotation nested in exisiting annotation t
          ;
        };
        Predicate<Vertex> hasDifferentUUID = t -> {
          String uuid2 = (String) t.property("uuid").value();
          return !uuid1.equals(uuid2);
        };
        overlaps.set(StreamUtil.stream(traversal)//
            .filter(hasDifferentUUID)//
            .filter(nonNestingOverlapWithAnnotation)//
            .findAny()//
            .isPresent()//
        );

      });
      return overlaps.get();
    });
  }

  @Override
  public TextRangeAnnotationList readTextRangeAnnotations(UUID resourceUUID) {
    return storage.runInTransaction(() -> getTextRangeAnnotationList(resourceUUID));
  }

  @Override
  public boolean storeTextGraph(UUID resourceId, ParseResult result) {
    if (readResource(resourceId).isPresent()) {
      textGraphService.storeTextGraph(resourceId, result);
      return true;
    }
    // something went wrong
    readResource(resourceId).get().setHasText(false);
    return false;
  }

  @Override
  public Stream<TextGraphSegment> getTextGraphSegmentStream(UUID resourceId, List<List<String>> orderedLayerTags) {
    return textGraphService.getTextGraphSegmentStream(resourceId, orderedLayerTags);
  }

  @Override
  public Stream<TextAnnotation> getTextAnnotationStream(UUID resourceId) {
    return textGraphService.getTextAnnotationStream(resourceId);
  }

  @Override
  public void updateTextAnnotation(TextAnnotation textAnnotation) {
    textGraphService.updateTextAnnotation(textAnnotation);
  }

  @Override
  public void wrapContentInChildTextAnnotation(TextAnnotation existingTextAnnotation, TextAnnotation newTextAnnotation) {
    textGraphService.wrapContentInChildTextAnnotation(existingTextAnnotation, newTextAnnotation);
  }

  @Override
  public void setTextView(UUID resourceUUID, String viewId, TextView textView, TextViewDefinition textViewDefinition) {
    storage.runInTransaction(() -> {
      ResourceVF resourceVF = storage.readVF(ResourceVF.class, resourceUUID).get();
      String json;
      try {
        String serializedTextViewMap = resourceVF.getSerializedTextViewMap();
        Map<String, TextView> textViewMap = deserializeToTextViewMap(serializedTextViewMap);
        textViewMap.put(viewId, textView);
        json = serializeTextViewMap(textViewMap);
        resourceVF.setSerializedTextViewMap(json);

        String serializedTextViewDefinitionMap = resourceVF.getSerializedTextViewDefinitionMap();
        Map<String, TextViewDefinition> textViewDefinitionMap = deserializeToTextViewDefinitionMap(serializedTextViewDefinitionMap);
        textViewDefinitionMap.put(viewId, textViewDefinition);
        json = serializeTextViewDefinitionMap(textViewDefinitionMap);
        resourceVF.setSerializedTextViewDefinitionMap(json);

      } catch (Exception e) {
        e.printStackTrace();
        throw new RuntimeException(e);
      }
    });
  }

  @Override
  public List<TextView> getTextViewsForResource(UUID resourceUUID) {
    List<TextView> textViews = new ArrayList<>();
    Set<String> viewNames = Sets.newHashSet();

    return storage.runInTransaction(() -> {
      ResourceVF resourceVF = storage.readVF(ResourceVF.class, resourceUUID).get();
      while (resourceVF != null) {
        String serializedTextViews = resourceVF.getSerializedTextViewMap();
        UUID uuid = UUID.fromString(resourceVF.getUuid());
        try {
          deserializeToTextViews(serializedTextViews).stream().filter(v -> !viewNames.contains(v.getName())).forEach((tv) -> {
            tv.setTextViewDefiningResourceId(uuid);
            textViews.add(tv);
            viewNames.add(tv.getName());
          });

        } catch (Exception e) {
          e.printStackTrace();
          throw new RuntimeException(e);
        }
        resourceVF = resourceVF.getParentResource();
      }
      return textViews;
    });
  }

  @Override
  public Optional<TextView> getTextView(UUID resourceId, String view) {
    TextView textView = storage.runInTransaction(() -> {
      ResourceVF resourceVF = storage.readVF(ResourceVF.class, resourceId).get();
      String serializedTextViews = resourceVF.getSerializedTextViewMap();
      try {
        Map<String, TextView> textViewMap = deserializeToTextViewMap(serializedTextViews);
        List<TextView> textViews = textViewMap//
            .entrySet()//
            .stream()//
            .filter(e -> e.getKey().equals(view))//
            .map(this::setName)//
            .collect(toList());

        if (textViews.isEmpty()) {
          ResourceVF parentResourceVF = resourceVF.getParentResource();
          if (parentResourceVF != null) {
            UUID parentUUID = UUID.fromString(parentResourceVF.getUuid());
            return getTextView(parentUUID, view).orElse(null);
          } else {
            return null;
          }

        } else {
          return textViews.get(0);
        }

      } catch (Exception e) {
        e.printStackTrace();
        throw new RuntimeException(e);
      }
    });
    return Optional.ofNullable(textView);
  }

  @Override
  public Optional<TextViewDefinition> getTextViewDefinition(UUID resourceId, String view) {
    TextViewDefinition textViewDefinition = storage.runInTransaction(() -> {
      ResourceVF resourceVF = storage.readVF(ResourceVF.class, resourceId).get();
      String serializedTextViewDefinitions = resourceVF.getSerializedTextViewDefinitionMap();
      try {
        Map<String, TextViewDefinition> textViewDefinitionMap = deserializeToTextViewDefinitionMap(serializedTextViewDefinitions);
        return textViewDefinitionMap.get(view);

      } catch (Exception e) {
        e.printStackTrace();
        throw new RuntimeException(e);
      }
    });
    return Optional.ofNullable(textViewDefinition);
  }

  private AnnotatorVF frameAnnotator(Annotator annotator) {
    AnnotatorVF avf = storage.createVF(AnnotatorVF.class);
    avf.setCode(annotator.getCode());
    avf.setDescription(annotator.getDescription());
    return avf;
  }

  private Annotator deframeAnnotator(AnnotatorVF avf) {
    return new Annotator()//
        .setCode(avf.getCode())//
        .setDescription(avf.getDescription())//
        .setResourceURI(locationBuilder.locationOf(AlexandriaResource.class, avf.getResource().getUuid()));
  }

  private void updateTextRangeAnnotation(TextRangeAnnotationVF vf, TextRangeAnnotation annotation) {
    vf.setUuid(annotation.getId().toString());
    vf.setRevision(annotation.getRevision());
    vf.setName(annotation.getName());
    vf.setAnnotatorCode(annotation.getAnnotator());
    Position position = annotation.getPosition();
    position.getXmlId().ifPresent(xmlId -> {
      vf.setXmlId(xmlId);
      if (position.getOffset().isPresent()) {
        vf.setOffset(position.getOffset().get());
      }
      if (position.getLength().isPresent()) {
        vf.setLength(position.getLength().get());
      }
    });
    position.getTargetAnnotationId().ifPresent(targetId -> vf.setTargetAnnotationId(targetId.toString()));
    AbsolutePosition absolutePosition = annotation.getAbsolutePosition();
    vf.setAbsoluteXmlId(absolutePosition.getXmlId());
    vf.setAbsoluteOffset(absolutePosition.getOffset());
    vf.setAbsoluteLength(absolutePosition.getLength());
    vf.setUseOffset(annotation.getUseOffset());
    try {
      String json = new ObjectMapper().writeValueAsString(annotation.getAttributes());
      vf.setAttributesAsJson(json);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }

  }

  private TextRangeAnnotation deframeTextRangeAnnotation(TextRangeAnnotationVF vf) {
    String targetAnnotationId = vf.getTargetAnnotationId();
    UUID targetAnnotationUUID = targetAnnotationId == null ? null : UUID.fromString(targetAnnotationId);
    Position position = new Position()//
        .setTargetAnnotationId(targetAnnotationUUID)//
        .setXmlId(vf.getXmlId())//
        .setOffset(vf.getOffset())//
        .setLength(vf.getLength());
    AbsolutePosition absolutePosition = new AbsolutePosition()//
        .setXmlId(vf.getAbsoluteXmlId())//
        .setOffset(vf.getAbsoluteOffset())//
        .setLength(vf.getAbsoluteLength());
    Map<String, String> attributes;
    try {
      String attributesAsJson = StringUtils.defaultIfBlank(vf.getAttributesAsJson(), "{}");
      TypeReference<HashMap<String, String>> typeRef = new TypeReference<HashMap<String, String>>() {
      };
      attributes = new ObjectMapper().readValue(attributesAsJson, typeRef);
      return new TextRangeAnnotation()//
          .setId(getUUID(vf))//
          .setRevision(vf.getRevision())//
          .setName(vf.getName())//
          .setAnnotator(vf.getAnnotatorCode())//
          .setAttributes(attributes)//
          .setPosition(position)//
          .setAbsolutePosition(absolutePosition);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void setTextViews(ResourceVF rvf, AlexandriaResource resource) {
    String textViewsJson = rvf.getSerializedTextViewMap();
    if (StringUtils.isNotEmpty(textViewsJson)) {
      try {
        List<TextView> textViews = deserializeToTextViews(textViewsJson);
        setDirectTextViews(resource, textViews);
      } catch (IOException e) {
        e.printStackTrace();
        throw new RuntimeException(e);
      }
    }
  }
  // public List<TextView> getDirectTextViews() {
  // return directTextViews;
  // }
  //
  // public void setDirectTextViews(List<TextView> textViews) {
  // this.directTextViews = textViews;
  // }

  private void setDirectTextViews(AlexandriaResource resource, List<TextView> textViews) {

  }

  private List<TextView> deserializeToTextViews(String textViewsJson) throws IOException {
    Map<String, TextView> textViewMap = deserializeToTextViewMap(textViewsJson);
    return textViewMap.entrySet()//
        .stream()//
        .map(this::setName)//
        .collect(toList());
  }

  private TextView setName(Entry<String, TextView> entry) {
    TextView textView = entry.getValue();
    textView.setName(entry.getKey());
    return textView;
  }

  private String serializeTextViewMap(Map<String, TextView> textViewMap) throws JsonProcessingException {
    return TEXTVIEW_WRITER.writeValueAsString(textViewMap);
  }

  private Map<String, TextView> deserializeToTextViewMap(String json) throws IOException {
    if (StringUtils.isEmpty(json)) {
      return Maps.newHashMap();
    }
    return TEXTVIEW_READER.readValue(json);
  }

  private String serializeTextViewDefinitionMap(Map<String, TextViewDefinition> textViewDefinitionMap) throws JsonProcessingException {
    return TEXTVIEWDEFINITION_WRITER.writeValueAsString(textViewDefinitionMap);
  }

  private Map<String, TextViewDefinition> deserializeToTextViewDefinitionMap(String json) throws JsonProcessingException, IOException {
    if (StringUtils.isEmpty(json)) {
      return Maps.newHashMap();
    }
    return TEXTVIEWDEFINITION_READER.readValue(json);
  }
}
