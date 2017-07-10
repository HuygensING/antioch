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

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.util.UUID;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import nl.knaw.huygens.alexandria.client.model.AnnotationPrototype;
import nl.knaw.huygens.alexandria.client.model.ResourcePrototype;
import nl.knaw.huygens.alexandria.client.model.SubResourcePrototype;

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

  // TODO move to Markup module
  // protected TextImportStatus setResourceText(UUID resourceUuid, String xml) {
  // RestResult<Void> result = client.setResourceText(resourceUuid, xml);
  // assertThat(result).isNotNull();
  // assertThat(result.hasFailed()).isFalse();
  //
  // TextImportStatus textGraphImportStatus = null;
  // boolean goOn = true;
  // while (goOn) {
  // try {
  // Thread.sleep(1000);
  // } catch (InterruptedException e) {
  // e.printStackTrace();
  // }
  // RestResult<TextImportStatus> result2 = client.getTextImportStatus(resourceUuid);
  // assertThat(result2.hasFailed()).isFalse();
  // textGraphImportStatus = result2.get();
  // goOn = !textGraphImportStatus.isDone();
  // }
  // return textGraphImportStatus;
  // }

  protected UUID annotateResource(UUID resourceUuid, String annotationType, String annotationValue) {
    AnnotationPrototype annotationPrototype = new AnnotationPrototype()//
        .setType(annotationType)//
        .setValue(annotationValue);
    RestResult<UUID> result = client.annotateResource(resourceUuid, annotationPrototype);
    assertRequestSucceeded(result);
    return result.get();
  }

}
