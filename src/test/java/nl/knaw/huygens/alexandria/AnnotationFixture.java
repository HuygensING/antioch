package nl.knaw.huygens.alexandria;

import static org.concordion.api.MultiValueResult.multiValueResult;

import java.util.Map;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import org.concordion.api.MultiValueResult;
import org.concordion.integration.junit4.ConcordionRunner;
import org.junit.runner.RunWith;

@RunWith(ConcordionRunner.class)
public class AnnotationFixture {

  private final Map<String, String> annotatedReferences = Maps.newHashMap();

  public void setUpAnnotation(String id, String tag) {
    annotatedReferences.put(id, tag);
  }

  public String addAnnotation(String tag) {
    return addAnnotation(null, tag);
  }

  public String addAnnotation(String id, String tag) {
    if (Strings.isNullOrEmpty(id)) {
      return "400 Bad Request";
    }

    if (annotatedReferences.containsKey(id)) {
      return "409 Conflict";
    }

    annotatedReferences.put(id, tag);
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
