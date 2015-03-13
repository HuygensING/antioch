package nl.knaw.huygens.alexandria.external;

import static org.mockito.Matchers.anyObject;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import nl.knaw.huygens.alexandria.exception.IllegalResourceException;
import nl.knaw.huygens.alexandria.util.UUIDParser;
import org.concordion.integration.junit4.ConcordionRunner;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

@RunWith(ConcordionRunner.class)
public class CreationFixture extends ResourcesFixture {
  @Override
  public void request(String method, String path) {
//    when(resourceService().createResource(any(AlexandriaResource.class))).then(returnsFirstArg());
    System.err.println(String.format("CreationFixture.request(%s,%s)", method, path));

    Mockito.when(resourceService().createResource(anyObject()))
           .thenThrow(new IllegalResourceException(Response.status(Status.BAD_REQUEST).entity("asjemenou!").build()));

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
