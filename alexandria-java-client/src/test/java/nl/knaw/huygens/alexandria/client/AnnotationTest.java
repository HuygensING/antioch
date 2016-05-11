package nl.knaw.huygens.alexandria.client;

import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import nl.knaw.huygens.alexandria.client.model.AnnotationPojo;
import nl.knaw.huygens.alexandria.client.model.AnnotationPrototype;

public class AnnotationTest extends AlexandriaClientTest {
  @Before
  public void before() {
    client.setAuthKey(AUTHKEY);
    client.setAutoConfirm(true);
  }

  @Test
  public void testAnnotatingAResource() {
    UUID resourceUuid = createResource("resource");

    // annotate the resource
    String annotationType = "userRemark";
    String annotationValue = "WTF?";
    UUID annotationUuid = annotateResource(resourceUuid, annotationType, annotationValue);

    AnnotationPojo annotationPojo = readAnnotation(annotationUuid);
    softly.assertThat(annotationPojo.getType()).as("type").isEqualTo(annotationType);
    softly.assertThat(annotationPojo.getValue()).as("value").isEqualTo(annotationValue);

    AnnotationPojo annotationPojo2 = readAnnotationRevision(annotationUuid, 0);
    softly.assertThat(annotationPojo2.getType()).as("type").isEqualTo(annotationType);
    softly.assertThat(annotationPojo2.getValue()).as("value").isEqualTo(annotationValue);
  }

  @Test
  public void testAnnotatingAnAnnotation() {
    UUID resourceUuid = createResource("resource");

    // annotate the resource
    String annotationType1 = "userRemark";
    String annotationValue1 = "WTF?";
    UUID annotationUuid = annotateResource(resourceUuid, annotationType1, annotationValue1);

    // annotate the annotation
    String annotationType2 = "editorRemark";
    String annotationValue2 = "language, please!";
    UUID annotationUuid2 = annotateAnnotation(annotationUuid, annotationType2, annotationValue2);

    AnnotationPojo annotationPojo = readAnnotation(annotationUuid2);
    softly.assertThat(annotationPojo.getType()).as("type").isEqualTo(annotationType2);
    softly.assertThat(annotationPojo.getValue()).as("value").isEqualTo(annotationValue2);
  }

  private UUID annotateResource(UUID resourceUuid, String annotationType, String annotationValue) {
    AnnotationPrototype annotationPrototype = new AnnotationPrototype()//
        .setType(annotationType)//
        .setValue(annotationValue);
    RestResult<UUID> result = client.annotateResource(resourceUuid, annotationPrototype);
    assertRequestSucceeded(result);
    UUID annotationUuid = result.get();
    return annotationUuid;
  }

  private AnnotationPojo readAnnotation(UUID annotationUuid) {
    RestResult<AnnotationPojo> result = client.getAnnotation(annotationUuid);
    assertRequestSucceeded(result);
    AnnotationPojo annotationPojo = result.get();

    softly.assertThat(annotationPojo).isNotNull();
    return annotationPojo;
  }

  private AnnotationPojo readAnnotationRevision(UUID annotationUuid, Integer revision) {
    RestResult<AnnotationPojo> result = client.getAnnotationRevision(annotationUuid, revision);
    assertRequestSucceeded(result);
    AnnotationPojo annotationPojo = result.get();

    softly.assertThat(annotationPojo).isNotNull();
    return annotationPojo;
  }

  private UUID annotateAnnotation(UUID resourceUuid, String annotationType, String annotationValue) {
    AnnotationPrototype annotationPrototype = new AnnotationPrototype()//
        .setType(annotationType)//
        .setValue(annotationValue);
    RestResult<UUID> result2 = client.annotateAnnotation(resourceUuid, annotationPrototype);
    assertRequestSucceeded(result2);
    return result2.get();
  }
}
