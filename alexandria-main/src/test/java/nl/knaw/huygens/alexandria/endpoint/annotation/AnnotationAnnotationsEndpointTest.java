package nl.knaw.huygens.alexandria.endpoint.annotation;

/*
 * #%L
 * alexandria-main
 * =======
 * Copyright (C) 2015 - 2016 Huygens ING (KNAW)
 * =======
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import javax.ws.rs.core.Response;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.ImmutableSet;

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
    when(annotation0.isActive()).thenReturn(true);

    AlexandriaAnnotationBody body1 = mock(AlexandriaAnnotationBody.class);
    when(body1.getType()).thenReturn("Comment");
    when(body1.getValue()).thenReturn("Improbable");

    UUID uuid1 = UUID.randomUUID();
    AlexandriaAnnotation annotation1 = mock(AlexandriaAnnotation.class);
    when(annotation1.getId()).thenReturn(uuid1);
    when(annotation1.getBody()).thenReturn(body1);
    when(annotation1.isActive()).thenReturn(true);

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
    Map<String, Object> entity = (Map<String, Object>) response.getEntity();
    Log.info("entity={}", entity);
    // ObjectMapper om = new ObjectMapper();
    // Log.info("json={}", om.writeValueAsString(entity));
    assertThat(entity).containsKeys("annotations");

    @SuppressWarnings("unchecked")
    Set<AnnotationEntity> list = (Set<AnnotationEntity>) entity.get("annotations");
    assertThat(list).hasSize(1);
    assertThat(list.iterator().next().getId()).isEqualTo(uuid1);
  }
}
