package nl.knaw.huygens.alexandria.annotation;

import static org.concordion.api.MultiValueResult.multiValueResult;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import nl.knaw.huygens.alexandria.endpoint.Annotations;
import nl.knaw.huygens.alexandria.exception.NotFoundException;
import nl.knaw.huygens.alexandria.exception.ResourceExistsException;
import nl.knaw.huygens.alexandria.helpers.ApiFixture;
import nl.knaw.huygens.alexandria.model.AlexandriaAnnotation;
import nl.knaw.huygens.alexandria.service.AnnotationService;
import org.concordion.api.MultiValueResult;
import org.concordion.integration.junit4.ConcordionRunner;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(ConcordionRunner.class)
public class AnnotationFixture extends ApiFixture {
  private static final Logger LOG = LoggerFactory.getLogger(AnnotationFixture.class);

  private static AnnotationService ANNOTATION_SERVICE_MOCK = mock(AnnotationService.class);

  private final Splitter COMMA_SPLITTER = Splitter.on(',');

  private final Map<String, List<String>> annotatedReferences = Maps.newHashMap();

  @BeforeClass
  public static void setup() {
    addClass(Annotations.class);
    addProviderForContext(AnnotationService.class, ANNOTATION_SERVICE_MOCK);
  }

  @Override
  public void clear() {
    super.clear();
    Mockito.reset(ANNOTATION_SERVICE_MOCK);
  }

  protected AnnotationService annotationService() {
    return ANNOTATION_SERVICE_MOCK;
  }

  public void noSuchAnnotation(String id) {
    UUID uuid = UUID.fromString(id);
    when(annotationService().readAnnotation(uuid)).thenThrow(new NotFoundException());
  }

  public void createAnnotation(String key, String value) throws ResourceExistsException {
    LOG.trace("createAnnotation([{}],[{}])", key, value);

    final AlexandriaAnnotation annotation = new AlexandriaAnnotation(UUID.randomUUID(), key, value);
    annotation.addAnnotation(new AlexandriaAnnotation(UUID.randomUUID(), "some", "value"));
    when(annotationService().createAnnotation(anyString(), anyString())).thenReturn(annotation);
  }

  public void createAnnotation(String id, String key, String value) throws ResourceExistsException {
    LOG.trace("createAnnotation([{}],[{}],[{}])", new Object[]{id, key, value});

    when(annotationService().createAnnotation(any(UUID.class), anyString(), anyString()))
        .thenReturn(new AlexandriaAnnotation(UUID.fromString(id), key, value));
  }

  public void setUpAnnotation(String id, String tag) {
    List<String> tags = annotatedReferences.get(id);

    if (tags == null) {
      tags = Lists.newArrayList();
      annotatedReferences.put(id, tags);
    }

    tags.add(tag);
  }

  public String addAnnotation(String id, String tag) {
    if (Strings.isNullOrEmpty(id) || Strings.isNullOrEmpty(tag)) {
      return "400 Bad Request";
    }

    List<String> tags = annotatedReferences.get(id);
    if (tags == null) {
      tags = Lists.newArrayList();
      annotatedReferences.put(id, tags);
    }

    if (tags.contains(tag)) {
      return "409 Conflict";
    }

    tags.add(tag);
    return "201 Created";
  }

  public MultiValueResult getAnnotation(String id) {
    if (Strings.isNullOrEmpty(id)) {
      return multiValueResult().with("status", "400 Bad Request");
    }

    if (annotatedReferences.containsKey(id)) {
      return multiValueResult().with("status", "200 Ok").with("value", annotatedReferences.get(id));
    }

    return multiValueResult().with("status", "404 Not Found");
  }
}
