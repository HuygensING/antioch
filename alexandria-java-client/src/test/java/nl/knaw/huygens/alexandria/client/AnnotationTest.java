package nl.knaw.huygens.alexandria.client;

/*
 * #%L
 * alexandria-java-client
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
