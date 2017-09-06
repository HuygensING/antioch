package nl.knaw.huygens.antioch.endpoint.annotation;

/*
 * #%L
 * antioch-main
 * =======
 * Copyright (C) 2015 - 2017 Huygens ING (KNAW)
 * =======
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import javax.ws.rs.core.Response;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.ImmutableSet;

import nl.knaw.huygens.Log;
import nl.knaw.huygens.antioch.config.AntiochConfiguration;
import nl.knaw.huygens.antioch.config.MockConfiguration;
import nl.knaw.huygens.antioch.endpoint.AnnotationCreationRequestBuilder;
import nl.knaw.huygens.antioch.endpoint.EndpointPathResolver;
import nl.knaw.huygens.antioch.endpoint.LocationBuilder;
import nl.knaw.huygens.antioch.endpoint.UUIDParam;
import nl.knaw.huygens.antioch.model.AntiochAnnotation;
import nl.knaw.huygens.antioch.model.AntiochAnnotationBody;
import nl.knaw.huygens.antioch.service.AntiochService;

public class AnnotationAnnotationsEndpointTest {
  @Test
  public void testAnnotationURIsAreCorrect() throws JsonProcessingException {
    AntiochAnnotationBody body0 = mock(AntiochAnnotationBody.class);
    when(body0.getType()).thenReturn("");
    when(body0.getValue()).thenReturn("Bookmark");

    UUID uuid0 = UUID.randomUUID();
    AntiochAnnotation annotation0 = mock(AntiochAnnotation.class);
    when(annotation0.getId()).thenReturn(uuid0);
    when(annotation0.getBody()).thenReturn(body0);
    when(annotation0.isActive()).thenReturn(true);

    AntiochAnnotationBody body1 = mock(AntiochAnnotationBody.class);
    when(body1.getType()).thenReturn("Comment");
    when(body1.getValue()).thenReturn("Improbable");

    UUID uuid1 = UUID.randomUUID();
    AntiochAnnotation annotation1 = mock(AntiochAnnotation.class);
    when(annotation1.getId()).thenReturn(uuid1);
    when(annotation1.getBody()).thenReturn(body1);
    when(annotation1.isActive()).thenReturn(true);

    // annotation1 is an annotation on annotation0
    when(annotation0.getAnnotations()).thenReturn(ImmutableSet.of(annotation1));

    AntiochService service = mock(AntiochService.class);
    Optional<AntiochAnnotation> optional0 = Optional.of(annotation0);
    Optional<AntiochAnnotation> optional1 = Optional.of(annotation1);
    when(service.readAnnotation(uuid0)).thenReturn(optional0);
    when(service.readAnnotation(uuid1)).thenReturn(optional1);

    AnnotationCreationRequestBuilder requestBuilder = mock(AnnotationCreationRequestBuilder.class);
    AntiochConfiguration configuration = new MockConfiguration();
    LocationBuilder locationBuilder = new LocationBuilder(configuration, new EndpointPathResolver());
    UUIDParam uuidParam = new UUIDParam(uuid0.toString());
    AnnotationAnnotationsEndpoint aa = new AnnotationAnnotationsEndpoint(service, requestBuilder, locationBuilder, uuidParam);

    Response response = aa.get();
    Log.info("response={}", response);

    List<AnnotationEntity> list = extractList(response);
    assertThat(list).hasSize(1);
    // assertThat(list.iterator().next().getId()).isEqualTo(uuid1);
  }

  private List<AnnotationEntity> extractList(Response response) {
    @SuppressWarnings("unchecked")
    Map<String, Object> entity = (Map<String, Object>) response.getEntity();
    Log.info("entity={}", entity);
    // ObjectMapper om = new ObjectMapper();
    // Log.info("json={}", om.writeValueAsString(entity));
    assertThat(entity).containsKeys("annotations");

    @SuppressWarnings("unchecked")
    List<AnnotationEntity> list = (List<AnnotationEntity>) entity.get("annotations");
    return list;
  }
}
