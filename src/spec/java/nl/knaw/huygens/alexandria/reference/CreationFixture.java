package nl.knaw.huygens.alexandria.reference;

import java.net.URI;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import nl.knaw.huygens.alexandria.RestFixture;
import nl.knaw.huygens.alexandria.UUIDValidator;
import org.concordion.api.MultiValueResult;
import org.concordion.integration.junit4.ConcordionRunner;
import org.junit.runner.RunWith;

@RunWith(ConcordionRunner.class)
public class CreationFixture extends RestFixture {
  private static String extractBaseURI(String s) {
    return s.substring(0, s.lastIndexOf('/') + 1);
  }

  private static String extractUUID(String s) {
    return Iterables.getLast(Splitter.on('/').split(s));
  }

  public MultiValueResult rest(String method, String path, String body) {
    final MultiValueResult result = invokeREST(method, path, body);

    location().map(URI::create).ifPresent(uri -> {
      result.with("locationPresent", "contains a Location header");
      result.with("locationScheme", uri.getScheme());
      final String locationPath = uri.getPath();
      result.with("locationStart", extractBaseURI(locationPath));
      final String uuid = extractUUID(locationPath);
      result.with("locationId", UUIDValidator.of(uuid).whenValid("well-formed UUID").orElse("malformed UUID: " + uuid));
    });

    return result;
  }

}
