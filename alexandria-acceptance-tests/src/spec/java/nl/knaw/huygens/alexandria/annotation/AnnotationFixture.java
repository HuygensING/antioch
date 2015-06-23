package nl.knaw.huygens.alexandria.annotation;

import static org.concordion.api.MultiValueResult.multiValueResult;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import nl.knaw.huygens.Log;
import nl.knaw.huygens.alexandria.endpoint.annotation.AnnotationsEndpoint;
import nl.knaw.huygens.alexandria.exception.NotFoundException;
import nl.knaw.huygens.alexandria.helpers.RestFixture;

import org.concordion.api.MultiValueResult;
import org.concordion.integration.junit4.ConcordionRunner;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

@RunWith(ConcordionRunner.class)
public class AnnotationFixture extends RestFixture {

  private final Map<String, List<String>> annotatedReferences = Maps.newHashMap();

  @BeforeClass
  public static void registerEndpoint() {
    Log.trace("Registering AnnotationsEndpoint");
    register(AnnotationsEndpoint.class);
  }

  public void noSuchAnnotation(String id) {
    when(service().readAnnotation(asUUID(id))).thenThrow(new NotFoundException());
  }

  // public void validAnnotation(String id) {
  // final UUID uuid = asUUID(id);
  //
  // final AlexandriaAnnotation annotation = new AlexandriaAnnotationBody(uuid, "aType", "aValue");
  // mockAnnotation.addAnnotation(annotation);
  //
  // Log.trace("Mocking annotationService.readAnnotation({}) -> [{}]", id, annotation);
  // when(annotationService().readAnnotation(uuid)).thenReturn(annotation);
  // }
  //
  // public void createAnnotation(String type, String value) {
  // Log.trace("createAnnotation([{}],[{}])", type, value);
  //
  // mockAnnotation = new AlexandriaAnnotation(randomUUID(), type, value);
  // mockAnnotation.setCreatedOn(Instant.now());
  // when(annotationService().createAnnotation(any(), any(), any())).thenReturn(mockAnnotation);
  // }

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
