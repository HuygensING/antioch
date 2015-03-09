package nl.knaw.huygens.alexandria.external;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;

import java.util.UUID;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import nl.knaw.huygens.alexandria.util.UUIDParser;
import org.concordion.integration.junit4.ConcordionRunner;
import org.junit.runner.RunWith;

@RunWith(ConcordionRunner.class)
public class CreationFixture extends ResourcesFixture {
  @Override
  public void request(String method, String path) {

    doReturn(UUID.randomUUID()).when(resourceService()).createResource(anyString());

    super.request(method, path);
  }

  public String base() {
    return baseOf(location());
  }

  public String uuidQuality() {
    String idStr = tailOf(location());
    return UUIDParser.fromString(idStr).get().map(uuid -> "well-formed UUID").orElse("malformed UUID: " + idStr);
  }

  private String baseOf(String s) {
    return s.substring(0, s.lastIndexOf('/') + 1);
  }

  private String tailOf(String s) {
    return Iterables.getLast(Splitter.on('/').split(s));
  }
}
