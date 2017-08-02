package nl.knaw.huygens.alexandria.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import nl.knaw.huygens.alexandria.api.model.Annotator;
import nl.knaw.huygens.alexandria.api.model.AnnotatorList;
import nl.knaw.huygens.alexandria.api.model.text.TextRangeAnnotation;
import nl.knaw.huygens.alexandria.api.model.text.TextRangeAnnotationList;
import nl.knaw.huygens.alexandria.api.model.text.view.TextView;
import nl.knaw.huygens.alexandria.api.model.text.view.TextViewDefinition;
import nl.knaw.huygens.alexandria.textgraph.ParseResult;
import nl.knaw.huygens.alexandria.textgraph.TextAnnotation;
import nl.knaw.huygens.alexandria.textgraph.TextGraphSegment;

public interface IMarkupService {
  Stream<TextAnnotation> getTextAnnotationStream(UUID resourceId);

  void updateTextAnnotation(TextAnnotation textAnnotation);

  void wrapContentInChildTextAnnotation(TextAnnotation existingTextAnnotation, TextAnnotation newChildTextAnnotation);

  void setResourceAnnotator(UUID resourceUUID, Annotator annotator);

  Optional<Annotator> readResourceAnnotator(UUID resourceUUID, String annotatorCode);

  AnnotatorList readResourceAnnotators(UUID id);

  void setTextRangeAnnotation(UUID resourceUUID, TextRangeAnnotation annotation);

  TextRangeAnnotationList readTextRangeAnnotations(UUID resourceUUID);

  void deprecateTextRangeAnnotation(UUID annotationUUID, TextRangeAnnotation newTextRangeAnnotation);

  Optional<TextRangeAnnotation> readTextRangeAnnotation(UUID resourceUUID, UUID annotationUUID);

  Optional<TextRangeAnnotation> readTextRangeAnnotation(UUID resourceUUID, UUID annotationUUID, Integer revision);

  boolean nonNestingOverlapWithExistingTextRangeAnnotationForResource(TextRangeAnnotation annotation, UUID resourceUUID);

  void setTextView(UUID resourceUUID, String viewId, TextView textView, TextViewDefinition textViewDefinition);

  Optional<TextViewDefinition> getTextViewDefinition(UUID resourceId, String viewId);

  Optional<TextView> getTextView(UUID resourceId, String view);

  /**
   * Gets the textviews for the resource and all its ancestors
   */
  List<TextView> getTextViewsForResource(UUID resourceUUID);

  boolean storeTextGraph(UUID resourceId, ParseResult result);

  Stream<TextGraphSegment> getTextGraphSegmentStream(UUID resourceId, List<List<String>> orderedLayerTags);

}
