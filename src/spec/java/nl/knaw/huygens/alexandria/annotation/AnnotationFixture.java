package nl.knaw.huygens.alexandria.annotation;

import static org.concordion.api.MultiValueResult.multiValueResult;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import nl.knaw.huygens.alexandria.endpoint.Annotations;
import nl.knaw.huygens.alexandria.exception.NotFoundException;
import nl.knaw.huygens.alexandria.exception.ResourceExistsException;
import nl.knaw.huygens.alexandria.helpers.ApiFixture;
import nl.knaw.huygens.alexandria.service.AnnotationService;
import org.concordion.api.MultiValueResult;
import org.junit.BeforeClass;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    when(annotationService().getAnnotation(uuid)).thenThrow(new NotFoundException());
  }

  public MultiValueResult testAnno(String key, String value) throws ResourceExistsException {
    body(key + ":" + value);
    request("POST", "/annotations");
    request("GET", "/annotations/1");
    MultiValueResult response = new MultiValueResult();
    response.with("key", COMMA_SPLITTER.split(response()).iterator().next());
    response.with("value", Iterables.getLast(COMMA_SPLITTER.split(response())));
    return response;
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
