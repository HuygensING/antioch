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
import nl.knaw.huygens.alexandria.client.model.ProvenancePojo;
import nl.knaw.huygens.alexandria.client.model.ResourcePojo;
import nl.knaw.huygens.alexandria.client.model.ResourcePrototype;
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
    SubResourcePrototype subresource = new SubResourcePrototype().setSub("/some/sub/path");
    RestResult<UUID> result = client.addSubResource(resourceUuid, subresource);
    assertRequestSucceeded(result);
    UUID subresourceUuid = result.get();

    // retrieve the resource again
    RestResult<SubResourcePojo> result4 = client.getSubResource(subresourceUuid);
    assertRequestSucceeded(result4);
    SubResourcePojo resourcePojo2 = result4.get();
    softly.assertThat(resourcePojo2).isNotNull();
    softly.assertThat(resourcePojo2.getSub()).as("sub").isEqualTo("/some/sub/path");
    softly.assertThat(resourcePojo2.getState().getValue()).as("state").isEqualTo(AlexandriaState.CONFIRMED);
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
    ResourcePojo ResourcePojo = result2.get();
    softly.assertThat(ResourcePojo).as("entity != null").isNotNull();
    softly.assertThat(ResourcePojo.getRef()).as("ref").isEqualTo(ref);
    softly.assertThat(ResourcePojo.getState().getValue()).as("state").isEqualTo(AlexandriaState.CONFIRMED);
  }

}
