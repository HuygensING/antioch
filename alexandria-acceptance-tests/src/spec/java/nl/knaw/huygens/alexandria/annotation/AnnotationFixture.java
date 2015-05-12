package nl.knaw.huygens.alexandria.annotation;

import static org.concordion.api.MultiValueResult.multiValueResult;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.AbstractModule;
import com.google.inject.Module;
import nl.knaw.huygens.alexandria.endpoint.annotation.AnnotationCreationCommandBuilder;
import nl.knaw.huygens.alexandria.endpoint.annotation.AnnotationsEndpoint;
import nl.knaw.huygens.alexandria.exception.NotFoundException;
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

  private static final AnnotationService ANNOTATION_SERVICE_MOCK = mock(AnnotationService.class);

  private final Map<String, List<String>> annotatedReferences = Maps.newHashMap();
  private AlexandriaAnnotation mockAnnotation;

  @BeforeClass
  public static void setup() {
    setupJerseyAndGuice(resourceModule());
    register(AnnotationsEndpoint.class);
  }

  private static Module resourceModule() {
    return new AbstractModule() {
      @Override
      protected void configure() {
        LOG.trace("setting up Guice bindings");
        bind(AnnotationService.class).toInstance(ANNOTATION_SERVICE_MOCK);
        bind(AnnotationCreationCommandBuilder.class)
            .toInstance(AnnotationCreationCommandBuilder.servedBy(ANNOTATION_SERVICE_MOCK));
      }
    };
  }

  @Override
  public void clear() {
    LOG.trace("clear");
    super.clear();
    Mockito.reset(ANNOTATION_SERVICE_MOCK);
  }

  protected AnnotationService annotationService() {
    return ANNOTATION_SERVICE_MOCK;
  }

  public void noSuchAnnotation(String id) {
    when(annotationService().readAnnotation(asUUID(id))).thenThrow(new NotFoundException());
  }

  public void validAnnotation(String id) {
    final UUID uuid = asUUID(id);

    final AlexandriaAnnotation annotation = new AlexandriaAnnotation(uuid, "aType", "aValue");
    mockAnnotation.addAnnotation(annotation);

    LOG.trace("Mocking annotationService.readAnnotation({}) -> [{}]", id, annotation);
    when(annotationService().readAnnotation(uuid)).thenReturn(annotation);
  }

  public void createAnnotation(String type, String value) {
    LOG.trace("createAnnotation([{}],[{}])", type, value);

    mockAnnotation = new AlexandriaAnnotation(randomUUID(), type, value);
    mockAnnotation.setCreatedOn(Instant.now());
    when(annotationService().createAnnotation(any(), any(), any())).thenReturn(mockAnnotation);
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

  private UUID asUUID(String s) {
    return UUID.fromString(s);
  }

  private UUID randomUUID() {
    return UUID.randomUUID();
  }
}
