package nl.knaw.huygens.alexandria.external;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;

import java.net.URI;
import java.util.UUID;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import nl.knaw.huygens.alexandria.util.UUIDParser;
import org.concordion.api.MultiValueResult;
import org.concordion.integration.junit4.ConcordionRunner;
import org.junit.runner.RunWith;

@RunWith(ConcordionRunner.class)
public class CreationFixture extends ResourcesFixture {
  private static String extractBaseURI(String s) {
    return s.substring(0, s.lastIndexOf('/') + 1);
  }

  private static String tailOf(String s) {
    return Iterables.getLast(Splitter.on('/').split(s));
  }

  public MultiValueResult rest(String method, String path, String body) {
    doReturn(UUID.randomUUID()).when(referenceService()).createReference(anyString());

    final MultiValueResult result = invokeREST(method, path, body);

    location().map(URI::create).ifPresent(uri -> {
      result.with("locationPresent", "contains a Location header");
      result.with("locationScheme", uri.getScheme());
      final String locationPath = uri.getPath();
      result.with("locationPath", locationPath);
      result.with("locationStart", extractBaseURI(locationPath));

      final String idStr = tailOf(locationPath);
      result.with("locationId", UUIDParser.fromString(idStr).get().map(uuid -> "well-formed UUID")
                                          .orElse("malformed UUID: " + idStr));
    });

    return result;
  }

}
