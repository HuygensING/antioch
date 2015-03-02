package nl.knaw.huygens.alexandria.annotation;

import static org.concordion.api.MultiValueResult.multiValueResult;

import java.util.List;
import java.util.Map;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import nl.knaw.huygens.alexandria.RestFixture;
import org.concordion.api.MultiValueResult;
import org.concordion.integration.junit4.ConcordionRunner;
import org.junit.runner.RunWith;

@RunWith(ConcordionRunner.class)
public class AnnotationFixture extends RestFixture {
  private final Map<String, List<String>> annotatedReferences = Maps.newHashMap();

  public MultiValueResult testAnno(String key, String value) {
    invokeREST("POST", "/annotations", key + ":" + value);
    final MultiValueResult response = invokeREST("GET", "/annotations/1");
    String body = (String)response.get("body");
    response.with("key", Splitter.on(',').split(body).iterator().next());
    response.with("value", Iterables.getLast(Splitter.on(',').split(body)));
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
