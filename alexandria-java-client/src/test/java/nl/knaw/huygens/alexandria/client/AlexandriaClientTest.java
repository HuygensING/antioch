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

import nl.knaw.huygens.alexandria.client.model.AnnotationPrototype;
import nl.knaw.huygens.alexandria.client.model.ResourcePrototype;
import nl.knaw.huygens.alexandria.client.model.SubResourcePrototype;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import java.net.URI;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class AlexandriaClientTest extends AlexandriaTestWithTestServer {
  static final String AUTHKEY = "AUTHKEY";

  protected static URI testURI = URI.create("http://localhost:2016/");

  static AlexandriaClient client;

  @BeforeClass
  public static void startClient() {
    client = new AlexandriaClient(testURI);
  }

  @AfterClass
  public static void stopClient() {
    client.close();
  }

  void assertRequestSucceeded(RestResult<?> result) {
    assertThat(result).isNotNull();
    assertThat(result.hasFailed())//
        .as("Request went OK")//
        .withFailMessage("request failed: %s", result.getFailureCause().orElse("something you should never see"))//
        .isFalse();
  }

  protected UUID createResource(String resourceRef) {
    ResourcePrototype resource = new ResourcePrototype().setRef(resourceRef);
    UUID resourceUuid = UUID.randomUUID();
    RestResult<Void> result = client.setResource(resourceUuid, resource);
    assertRequestSucceeded(result);
    return resourceUuid;
  }

  protected UUID createSubResource(UUID resourceUuid, String ref) {
    SubResourcePrototype subresource = new SubResourcePrototype().setSub(ref);
    RestResult<UUID> result = client.addSubResource(resourceUuid, subresource);
    assertRequestSucceeded(result);
    return result.get();
  }

  protected UUID annotateResource(UUID resourceUuid, String annotationType, String annotationValue) {
    AnnotationPrototype annotationPrototype = new AnnotationPrototype()//
        .setType(annotationType)//
        .setValue(annotationValue);
    RestResult<UUID> result = client.annotateResource(resourceUuid, annotationPrototype);
    assertRequestSucceeded(result);
    return result.get();
  }

}
