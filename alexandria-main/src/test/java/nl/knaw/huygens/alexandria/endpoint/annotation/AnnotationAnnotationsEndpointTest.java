package nl.knaw.huygens.alexandria.endpoint.annotation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;
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
import nl.knaw.huygens.alexandria.model.AlexandriaAnnotationBody;
import nl.knaw.huygens.alexandria.service.AlexandriaService;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableSet;

public class AnnotationAnnotationsEndpointTest {
  @Test
  public void testAnnotationURIsAreCorrect() throws JsonProcessingException {

    AlexandriaAnnotationBody body0 = mock(AlexandriaAnnotationBody.class);
    when(body0.getType()).thenReturn("");
    when(body0.getValue()).thenReturn("Bookmark");

    UUID uuid0 = UUID.randomUUID();
    AlexandriaAnnotation annotation0 = mock(AlexandriaAnnotation.class);
    when(annotation0.getId()).thenReturn(uuid0);
    when(annotation0.getBody()).thenReturn(body0);

    AlexandriaAnnotationBody body1 = mock(AlexandriaAnnotationBody.class);
    when(body1.getType()).thenReturn("Comment");
    when(body1.getValue()).thenReturn("Improbable");

    UUID uuid1 = UUID.randomUUID();
    AlexandriaAnnotation annotation1 = mock(AlexandriaAnnotation.class);
    when(annotation1.getId()).thenReturn(uuid1);
    when(annotation1.getBody()).thenReturn(body1);

    // annotation1 is an annotation on annotation0
    when(annotation0.getAnnotations()).thenReturn(ImmutableSet.of(annotation1));

    AlexandriaService service = mock(AlexandriaService.class);
    Optional<AlexandriaAnnotation> optional0 = Optional.of(annotation0);
    Optional<AlexandriaAnnotation> optional1 = Optional.of(annotation1);
    when(service.readAnnotation(uuid0)).thenReturn(optional0);
    when(service.readAnnotation(uuid1)).thenReturn(optional1);

    AnnotationCreationRequestBuilder requestBuilder = mock(AnnotationCreationRequestBuilder.class);
    AlexandriaConfiguration configuration = new MockConfiguration();
    LocationBuilder locationBuilder = new LocationBuilder(configuration, new EndpointPathResolver());
    UUIDParam uuidParam = new UUIDParam(uuid0.toString());
    AnnotationAnnotationsEndpoint aa = new AnnotationAnnotationsEndpoint(service, requestBuilder, locationBuilder, uuidParam);

    Response response = aa.get();
    Log.info("response={}", response);

    @SuppressWarnings("unchecked")
    Set<AnnotationEntity> entity = (Set<AnnotationEntity>) response.getEntity();
    Log.info("entity={}", entity);
    ObjectMapper om = new ObjectMapper();
    Log.info("json={}", om.writeValueAsString(entity));
    assertThat(entity).hasSize(1);
    assertThat(entity.iterator().next().getId()).isEqualTo(uuid1);
  }
}
