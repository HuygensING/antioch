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

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import nl.knaw.huygens.alexandria.endpoint.UUIDParam;
import nl.knaw.huygens.alexandria.model.AlexandriaAnnotation;
import nl.knaw.huygens.alexandria.model.AlexandriaAnnotationBody;
import nl.knaw.huygens.alexandria.service.AlexandriaService;
import org.junit.Before;
import org.junit.Test;

public class OmittedOrMatchingTypeTest {
  private final UUIDParam uuidParam = new UUIDParam(UUID.randomUUID().toString());

  private AlexandriaService service;
  private AlexandriaAnnotation annotation;
  private OmittedOrMatchingType.Validator validator;
  private AnnotationPrototype mockPrototype;

  @Before
  public void given() {
    final AlexandriaAnnotationBody annotationBody = mock(AlexandriaAnnotationBody.class);
    when(annotationBody.getType()).thenReturn("some type");

    annotation = mock(AlexandriaAnnotation.class);
    when(annotation.getBody()).thenReturn(annotationBody);

    service = mock(AlexandriaService.class);
    when(service.readAnnotation(uuidParam.getValue())).thenReturn(Optional.of(annotation));

    validator = new OmittedOrMatchingType.Validator(service, uuidParam);

    mockPrototype = mock(AnnotationPrototype.class);
  }

  @Test
  public void testOmittingTypeInPrototypeShouldClassifyAsValid() {
    when(mockPrototype.getType()).thenReturn(Optional.empty());

    assertTrue("Omitting type in prototype should be valid", validator.isValid(mockPrototype, null));
  }

  @Test
  public void testEqualTypeInPrototypeShouldClassifyAsValid() {
    when(mockPrototype.getType()).thenReturn(Optional.of("some type"));

    assertTrue("Equal type in prototype should be valid", validator.isValid(mockPrototype, null));
  }

  @Test
  public void testDifferentTypeInPrototypeShouldClassifyAsInvalid() {
    when(mockPrototype.getType()).thenReturn(Optional.of("another type"));

    assertTrue("Different type in prototype should be invalid", !validator.isValid(mockPrototype, null));
  }

}
