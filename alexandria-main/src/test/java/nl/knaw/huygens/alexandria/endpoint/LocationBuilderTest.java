package nl.knaw.huygens.alexandria.endpoint;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.net.URI;
import java.util.UUID;

import nl.knaw.huygens.alexandria.config.MockConfiguration;
import nl.knaw.huygens.alexandria.model.AlexandriaAnnotation;
import nl.knaw.huygens.alexandria.model.AlexandriaAnnotationBody;
import nl.knaw.huygens.alexandria.model.AlexandriaResource;
import nl.knaw.huygens.alexandria.model.TentativeAlexandriaProvenance;

import org.junit.Test;

public class LocationBuilderTest {
  LocationBuilder lb = new LocationBuilder(new MockConfiguration(), new EndpointPathResolver());

  @Test
  public void testGetLocationOfAlexandriaAnnotationWithUUID() {
    UUID randomUUID = UUID.randomUUID();
    AlexandriaAnnotationBody body = mock(AlexandriaAnnotationBody.class);
    TentativeAlexandriaProvenance provenance = mock(TentativeAlexandriaProvenance.class);
    AlexandriaAnnotation annotation = new AlexandriaAnnotation(randomUUID, body, provenance);
    URI locationOf = lb.locationOf(annotation);
    assertThat(locationOf.toString()).isEqualTo("http://alexandria.org/annotations/" + randomUUID);
  }

  @Test
  public void testGetLocationOfAlexandriaResourceWithUUID() {
    UUID randomUUID = UUID.randomUUID();
    TentativeAlexandriaProvenance provenance = mock(TentativeAlexandriaProvenance.class);
    AlexandriaResource resource = new AlexandriaResource(randomUUID, provenance);
    URI locationOf = lb.locationOf(resource);
    assertThat(locationOf.toString()).isEqualTo("http://alexandria.org/resources/" + randomUUID);
  }
}
