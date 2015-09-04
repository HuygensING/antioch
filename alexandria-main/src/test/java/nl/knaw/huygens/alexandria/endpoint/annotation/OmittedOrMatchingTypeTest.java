package nl.knaw.huygens.alexandria.endpoint.annotation;

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