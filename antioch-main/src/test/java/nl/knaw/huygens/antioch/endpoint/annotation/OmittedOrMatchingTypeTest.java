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

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import nl.knaw.huygens.antioch.endpoint.UUIDParam;
import nl.knaw.huygens.antioch.model.AntiochAnnotation;
import nl.knaw.huygens.antioch.model.AntiochAnnotationBody;
import nl.knaw.huygens.antioch.service.AntiochService;
import org.junit.Before;
import org.junit.Test;

public class OmittedOrMatchingTypeTest {
  private final UUIDParam uuidParam = new UUIDParam(UUID.randomUUID().toString());

  private OmittedOrMatchingType.Validator validator;
  private AnnotationPrototype mockPrototype;

  @Before
  public void given() {
    final AntiochAnnotationBody annotationBody = mock(AntiochAnnotationBody.class);
    when(annotationBody.getType()).thenReturn("some type");

    AntiochAnnotation annotation = mock(AntiochAnnotation.class);
    when(annotation.getBody()).thenReturn(annotationBody);

    AntiochService service = mock(AntiochService.class);
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
