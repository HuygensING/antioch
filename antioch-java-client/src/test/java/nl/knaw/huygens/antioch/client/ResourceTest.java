package nl.knaw.huygens.antioch.client;

/*
 * #%L
 * antioch-java-client
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

import java.util.UUID;

import org.junit.Test;

import nl.knaw.huygens.Log;
import nl.knaw.huygens.antioch.api.model.AntiochState;
import nl.knaw.huygens.antioch.client.model.AnnotationList;
import nl.knaw.huygens.antioch.client.model.AnnotationPojo;
import nl.knaw.huygens.antioch.client.model.AnnotationPrototype;
import nl.knaw.huygens.antioch.client.model.ProvenancePojo;
import nl.knaw.huygens.antioch.client.model.ResourcePojo;
import nl.knaw.huygens.antioch.client.model.ResourcePrototype;
import nl.knaw.huygens.antioch.client.model.SubResourceList;
import nl.knaw.huygens.antioch.client.model.SubResourcePojo;
import nl.knaw.huygens.antioch.client.model.SubResourcePrototype;

public class ResourceTest extends AntiochClientTest {

  @Test
  public void testAddResourceReturnsValidUUID() {
    client.setAuthKey(AUTHKEY);
    client.setAutoConfirm(false);
    String resourceRef = "corpus";
    ResourcePrototype resource = new ResourcePrototype().setRef(resourceRef);
    RestResult<UUID> result = client.addResource(resource);
    assertRequestSucceeded(result);
    UUID resourceUuid = result.get();
    Log.info("resourceUUID = {}", resourceUuid);
    softly.assertThat(resourceUuid).isNotNull();

    // retrieve the resource
    RestResult<ResourcePojo> result2 = client.getResource(resourceUuid);
    assertRequestSucceeded(result2);
    ResourcePojo resourcePojo = result2.get();
    softly.assertThat(resourcePojo).isNotNull();
    softly.assertThat(resourcePojo.getRef()).as("ref").isEqualTo("corpus");
    softly.assertThat(resourcePojo.getState().getValue()).as("state").isEqualTo(AntiochState.TENTATIVE);

    // confirm the resource
    RestResult<Void> result3 = client.confirmResource(resourceUuid);
    assertRequestSucceeded(result3);

    // retrieve the resource again
    RestResult<ResourcePojo> result4 = client.getResource(resourceUuid);
    assertRequestSucceeded(result4);
    ResourcePojo resourcePojo2 = result4.get();
    softly.assertThat(resourcePojo2).isNotNull();
    softly.assertThat(resourcePojo2.getRef()).as("ref").isEqualTo("corpus");
    softly.assertThat(resourcePojo2.getState().getValue()).as("state").isEqualTo(AntiochState.CONFIRMED);
  }

  @Test
  public void testAddSubResourceReturnsValidUUID() {
    client.setAuthKey(AUTHKEY);
    client.setAutoConfirm(true);

    UUID resourceUuid = createResource("corpus");
    String ref = "/some/sub/path";
    UUID subresourceUuid = createSubResource(resourceUuid, ref);

    // retrieve the resource again
    RestResult<SubResourcePojo> getResult1 = client.getSubResource(subresourceUuid);
    assertRequestSucceeded(getResult1);
    SubResourcePojo subResourcePojo1 = getResult1.get();
    softly.assertThat(subResourcePojo1).isNotNull();
    softly.assertThat(subResourcePojo1.getSub()).as("sub").isEqualTo(ref);
    softly.assertThat(subResourcePojo1.getState().getValue()).as("state").isEqualTo(AntiochState.CONFIRMED);
  }

  @Test
  public void testSetSubResourceReturnsGivenUUID() {
    client.setAuthKey(AUTHKEY);
    client.setAutoConfirm(true);

    UUID resourceUuid = createResource("corpus");
    UUID subresourceUUID = UUID.randomUUID();
    String ref = "/some/sub/path";
    SubResourcePrototype subresource = new SubResourcePrototype().setSub(ref);
    RestResult<Void> setResult1 = client.setSubResource(resourceUuid, subresourceUUID, subresource);
    assertRequestSucceeded(setResult1);

    // retrieve the resource again
    RestResult<SubResourcePojo> getResult1 = client.getSubResource(subresourceUUID);
    assertRequestSucceeded(getResult1);
    SubResourcePojo subResourcePojo1 = getResult1.get();
    softly.assertThat(subResourcePojo1).isNotNull();
    softly.assertThat(subResourcePojo1.getSub()).as("sub").isEqualTo(ref);
    softly.assertThat(subResourcePojo1.getState().getValue()).as("state").isEqualTo(AntiochState.CONFIRMED);

    // update the subresource
    String ref2 = "/some/other/sub/path";
    SubResourcePrototype newSubresource = new SubResourcePrototype().setSub(ref2);
    RestResult<Void> setResult2 = client.setSubResource(resourceUuid, subresourceUUID, newSubresource);
    assertRequestSucceeded(setResult2);

    RestResult<SubResourcePojo> getResult2 = client.getSubResource(subresourceUUID);
    assertRequestSucceeded(getResult2);
    SubResourcePojo subResourcePojo2 = getResult2.get();
    softly.assertThat(subResourcePojo2).isNotNull();
    softly.assertThat(subResourcePojo2.getSub()).as("sub").isEqualTo(ref2);
    softly.assertThat(subResourcePojo2.getState().getValue()).as("state").isEqualTo(AntiochState.CONFIRMED);
  }

  @Test
  public void testAddResourceWithProvenanceReturnsValidUUID() {
    client.setAuthKey(AUTHKEY);
    ResourcePrototype resource = new ResourcePrototype()//
        .setRef("corpus2")//
        .setProvenance(new ProvenancePojo().setWho("test").setWhy("because test"));
    RestResult<UUID> result = client.addResource(resource);
    assertRequestSucceeded(result);
    UUID resourceUuid = result.get();
    Log.info("resourceUUID = {}", resourceUuid);
    assertThat(resourceUuid).isNotNull();
  }

  @Test
  public void testSetResourceWithProvidedUUID() {
    client.setAuthKey(AUTHKEY);
    String ref = "resource3";
    ResourcePrototype resource = new ResourcePrototype().setRef(ref).setProvenance(new ProvenancePojo().setWho("test3").setWhy("because test3"));
    UUID resourceId = UUID.fromString("11111111-1111-1111-1111-111111111111");
    RestResult<Void> result = client.setResource(resourceId, resource);
    assertRequestSucceeded(result);

    // retrieve the resource
    RestResult<ResourcePojo> result2 = client.getResource(resourceId);
    assertRequestSucceeded(result2);
    ResourcePojo resourcePojo = result2.get();
    softly.assertThat(resourcePojo).as("entity != null").isNotNull();
    softly.assertThat(resourcePojo.getRef()).as("ref").isEqualTo(ref);
    softly.assertThat(resourcePojo.getState().getValue()).as("state").isEqualTo(AntiochState.CONFIRMED);
  }

  @Test
  public void testGetAnnotationsFromResourceUUID() {
    client.setAuthKey(AUTHKEY);
    String ref = "resource4";
    ResourcePrototype resource = new ResourcePrototype().setRef(ref).setProvenance(new ProvenancePojo().setWho("test4").setWhy("because test4"));
    UUID resourceId = UUID.fromString("11111111-1111-1111-1111-111111111112");
    RestResult<Void> result = client.setResource(resourceId, resource);
    assertRequestSucceeded(result);

    // add annotations
    RestResult<UUID> annotateResourceResult1 = client.annotateResource(resourceId, new AnnotationPrototype().setType("typeUno").setValue("valueUno"));
    assertRequestSucceeded(annotateResourceResult1);
    AnnotationPojo annotation1 = client.getAnnotation(annotateResourceResult1.get()).get();

    RestResult<UUID> annotateResourceResult2 = client.annotateResource(resourceId, new AnnotationPrototype().setType("typeDos").setValue("valueDos"));
    assertRequestSucceeded(annotateResourceResult2);
    AnnotationPojo annotation2 = client.getAnnotation(annotateResourceResult2.get()).get();

    // retrieve the resource
    RestResult<AnnotationList> annotationsResult = client.getResourceAnnotations(resourceId);
    assertRequestSucceeded(annotationsResult);
    AnnotationList annotationList = annotationsResult.get();
    softly.assertThat(annotationList).as("entity != null").isNotNull();
    softly.assertThat(annotationList).hasSize(2);
    softly.assertThat(annotationList).containsExactly(annotation2, annotation1);
  }

  @Test
  public void testGetSubresourcesFromResourceUUID() {
    client.setAuthKey(AUTHKEY);
    String ref = "resource5";
    ResourcePrototype resource = new ResourcePrototype().setRef(ref).setProvenance(new ProvenancePojo().setWho("test5").setWhy("because test5"));
    UUID resourceId = UUID.fromString("11111111-1111-1111-1111-111111111113");
    RestResult<Void> result = client.setResource(resourceId, resource);
    assertRequestSucceeded(result);

    // add subresources
    RestResult<UUID> subResourceResult1 = client.addSubResource(resourceId, new SubResourcePrototype().setSub("Uno"));
    assertRequestSucceeded(subResourceResult1);
    SubResourcePojo subresource1 = client.getSubResource(subResourceResult1.get()).get();

    RestResult<UUID> subResourceResult2 = client.addSubResource(resourceId, new SubResourcePrototype().setSub("Dos"));
    assertRequestSucceeded(subResourceResult2);
    SubResourcePojo subresource2 = client.getSubResource(subResourceResult2.get()).get();

    // retrieve the resource
    RestResult<SubResourceList> subresourcesResult = client.getSubResources(resourceId);
    assertRequestSucceeded(subresourcesResult);
    SubResourceList subresourceList = subresourcesResult.get();
    softly.assertThat(subresourceList).as("entity != null").isNotNull();
    softly.assertThat(subresourceList).hasSize(2);
    // sorted on sub, so Dos before Uno
    softly.assertThat(subresourceList).containsExactly(subresource2, subresource1);
  }
}
