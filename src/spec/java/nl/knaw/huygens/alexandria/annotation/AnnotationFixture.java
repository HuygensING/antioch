package nl.knaw.huygens.alexandria.annotation;

import static org.concordion.api.MultiValueResult.multiValueResult;

import java.util.List;
import java.util.Map;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import nl.knaw.huygens.alexandria.helpers.ApiFixture;
import nl.knaw.huygens.alexandria.resource.Annotations;
import org.concordion.api.MultiValueResult;
import org.concordion.integration.junit4.ConcordionRunner;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;

@RunWith(ConcordionRunner.class)
public class AnnotationFixture extends ApiFixture {
  private final Splitter COMMA_SPLITTER = Splitter.on(',');

  private final Map<String, List<String>> annotatedReferences = Maps.newHashMap();

  @BeforeClass
  public static void setup() {
    System.err.println("AnnotationFixture::setup");

    addClass(Annotations.class);
  }

  public MultiValueResult testAnno(String key, String value) {
    body(key + ":" + value);
    request("POST", "/annotations");
    request("GET", "/annotations/1");
    MultiValueResult response = new MultiValueResult();
    response.with("key", COMMA_SPLITTER.split(entity()).iterator().next());
    response.with("value", Iterables.getLast(COMMA_SPLITTER.split(entity())));
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
