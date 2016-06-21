package nl.knaw.huygens.alexandria.client;

/*
 * #%L
 * alexandria-java-client
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

import java.util.UUID;

import org.junit.Test;

import nl.knaw.huygens.Log;
import nl.knaw.huygens.alexandria.api.model.AlexandriaState;
import nl.knaw.huygens.alexandria.client.model.AnnotationList;
import nl.knaw.huygens.alexandria.client.model.AnnotationPojo;
import nl.knaw.huygens.alexandria.client.model.AnnotationPrototype;
import nl.knaw.huygens.alexandria.client.model.ProvenancePojo;
import nl.knaw.huygens.alexandria.client.model.ResourcePojo;
import nl.knaw.huygens.alexandria.client.model.ResourcePrototype;
import nl.knaw.huygens.alexandria.client.model.SubResourceList;
import nl.knaw.huygens.alexandria.client.model.SubResourcePojo;
import nl.knaw.huygens.alexandria.client.model.SubResourcePrototype;

public class ResourceTest extends AlexandriaClientTest {

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
    softly.assertThat(resourcePojo.getState().getValue()).as("state").isEqualTo(AlexandriaState.TENTATIVE);

    // confirm the resource
    RestResult<Void> result3 = client.confirmResource(resourceUuid);
    assertRequestSucceeded(result3);

    // retrieve the resource again
    RestResult<ResourcePojo> result4 = client.getResource(resourceUuid);
    assertRequestSucceeded(result4);
    ResourcePojo resourcePojo2 = result4.get();
    softly.assertThat(resourcePojo2).isNotNull();
    softly.assertThat(resourcePojo2.getRef()).as("ref").isEqualTo("corpus");
    softly.assertThat(resourcePojo2.getState().getValue()).as("state").isEqualTo(AlexandriaState.CONFIRMED);
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
    softly.assertThat(subResourcePojo1.getState().getValue()).as("state").isEqualTo(AlexandriaState.CONFIRMED);
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
    softly.assertThat(subResourcePojo1.getState().getValue()).as("state").isEqualTo(AlexandriaState.CONFIRMED);

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
    softly.assertThat(subResourcePojo2.getState().getValue()).as("state").isEqualTo(AlexandriaState.CONFIRMED);
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
    softly.assertThat(resourcePojo.getState().getValue()).as("state").isEqualTo(AlexandriaState.CONFIRMED);
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
