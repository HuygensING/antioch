package nl.knaw.huygens.alexandria.endpoint.annotation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Set;
import java.util.UUID;

import javax.ws.rs.core.Response;

import nl.knaw.huygens.Log;
import nl.knaw.huygens.alexandria.config.AlexandriaConfiguration;
import nl.knaw.huygens.alexandria.config.MockConfiguration;
import nl.knaw.huygens.alexandria.endpoint.AnnotationCreationRequestBuilder;
import nl.knaw.huygens.alexandria.endpoint.EndpointPathResolver;
import nl.knaw.huygens.alexandria.endpoint.LocationBuilder;
import nl.knaw.huygens.alexandria.endpoint.UUIDParam;
import nl.knaw.huygens.alexandria.model.AlexandriaAnnotation;
import nl.knaw.huygens.alexandria.model.AlexandriaResource;
import nl.knaw.huygens.alexandria.service.AlexandriaService;

import org.junit.Test;

import com.google.common.collect.ImmutableSet;

public class AnnotationAnnotationsTest {
  @Test
  public void testAnnotationURIsAreCorrect() {
    UUID uuid = UUID.randomUUID();

    AlexandriaAnnotation annotation = mock(AlexandriaAnnotation.class);
    when(annotation.getId()).thenReturn(uuid);

    AlexandriaResource resource = mock(AlexandriaResource.class);
    when(resource.getAnnotations()).thenReturn(ImmutableSet.of(annotation));

    AlexandriaService service = mock(AlexandriaService.class);
    when(service.readResource(uuid)).thenReturn(resource);

    AnnotationCreationRequestBuilder requestBuilder = mock(AnnotationCreationRequestBuilder.class);
    AlexandriaConfiguration configuration = new MockConfiguration();
    LocationBuilder locationBuilder = new LocationBuilder(configuration, new EndpointPathResolver());
    String uuidString = uuid.toString();
    UUIDParam uuidParam = new UUIDParam(uuidString);
    AnnotationAnnotations aa = new AnnotationAnnotations(service, requestBuilder, locationBuilder, uuidParam);

    Response response = aa.get();
    Log.info("response={}", response);

    Set<AnnotationEntity> entity = (Set<AnnotationEntity>) response.getEntity();
    Log.info("entity={}", entity);
    assertThat(entity).hasSize(1);
  }
}
