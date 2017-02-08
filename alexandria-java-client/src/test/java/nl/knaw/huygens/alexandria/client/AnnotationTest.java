package nl.knaw.huygens.alexandria.client;

/*
 * #%L
 * alexandria-java-client
 * =======
 * Copyright (C) 2015 - 2017 Huygens ING (KNAW)
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
