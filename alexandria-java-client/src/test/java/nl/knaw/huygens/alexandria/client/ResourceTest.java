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

import java.net.URI;
import java.util.UUID;

import org.junit.Test;

import nl.knaw.huygens.Log;
import nl.knaw.huygens.alexandria.api.EndpointPaths;
import nl.knaw.huygens.alexandria.api.model.AlexandriaState;
import nl.knaw.huygens.alexandria.api.model.ElementDefinition;
import nl.knaw.huygens.alexandria.api.model.TextView;
import nl.knaw.huygens.alexandria.api.model.TextViewPrototype;

public class ResourceTest extends AlexandriaClientTest {
  @Test
  public void testAddResourceReturnsValidUUID() {
    client.setAuthKey(AUTHKEY);
    client.setAutoConfirm(false);
    String resourceRef = "corpus";
    ResourcePrototype resource = new ResourcePrototype(resourceRef);
    RestResult<UUID> result = client.addResource(resource);
    assertRequestSucceeded(result);
    UUID resourceUuid = result.get();
    Log.info("resourceUUID = {}", resourceUuid);
    softly.assertThat(resourceUuid).isNotNull();

    // retrieve the resource
    RestResult<ResourceEntity> result2 = client.getResource(resourceUuid);
    assertRequestSucceeded(result2);
    ResourceEntity resourceEntity = result2.get();
    softly.assertThat(resourceEntity).isNotNull();
    softly.assertThat(resourceEntity.getRef()).as("ref").isEqualTo(resourceRef);
    softly.assertThat(resourceEntity.getState().getValue()).as("state").isEqualTo(AlexandriaState.TENTATIVE);

    // confirm the resource
    RestResult<Void> result3 = client.confirmResource(resourceUuid);
    assertRequestSucceeded(result3);

    // retrieve the resource again
    RestResult<ResourceEntity> result4 = client.getResource(resourceUuid);
    assertRequestSucceeded(result4);
    ResourceEntity resourceEntity2 = result4.get();
    softly.assertThat(resourceEntity2).isNotNull();
    softly.assertThat(resourceEntity2.getRef()).as("ref").isEqualTo(resourceRef);
    softly.assertThat(resourceEntity2.getState().getValue()).as("state").isEqualTo(AlexandriaState.CONFIRMED);
  }

  @Test
  public void testAddResourceWithProvenanceReturnsValidUUID() {
    client.setAuthKey(AUTHKEY);
    ResourcePrototype resource = new ResourcePrototype("corpus2").withProvenance(new ProvenancePrototype().setWho("test").setWhy("because test"));
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
    ResourcePrototype resource = new ResourcePrototype(ref).withProvenance(new ProvenancePrototype().setWho("test3").setWhy("because test3"));
    UUID resourceId = UUID.fromString("11111111-1111-1111-1111-111111111111");
    RestResult<Void> result = client.setResource(resourceId, resource);
    assertRequestSucceeded(result);

    // retrieve the resource
    RestResult<ResourceEntity> result2 = client.getResource(resourceId);
    assertRequestSucceeded(result2);
    ResourceEntity resourceEntity = result2.get();
    softly.assertThat(resourceEntity).as("entity != null").isNotNull();
    softly.assertThat(resourceEntity.getRef()).as("ref").isEqualTo(ref);
    softly.assertThat(resourceEntity.getState().getValue()).as("state").isEqualTo(AlexandriaState.CONFIRMED);
  }

  @Test
  public void testSetAndRetrieveTextView() {
    // first, create a resource
    client.setAuthKey(AUTHKEY);
    String ref = "corpus";
    ResourcePrototype resource = new ResourcePrototype(ref).withProvenance(new ProvenancePrototype().setWho("test").setWhy("because test"));
    UUID resourceId = UUID.randomUUID();
    RestResult<Void> result = client.setResource(resourceId, resource);
    assertRequestSucceeded(result);

    // then, set the base layer definition
    ElementDefinition baseElement1 = ElementDefinition.withName("body").withAttributes("lang");
    ElementDefinition baseElement2 = ElementDefinition.withName("p").withAttributes("n");
    TextViewPrototype textView = new TextViewPrototype().setIncludedElements(baseElement1, baseElement2).setIgnoredElements("note");
    RestResult<URI> result2 = client.addTextView(resourceId, textView);
    assertRequestSucceeded(result2);
    softly.assertThat(result2.get().toString()).as("Location").endsWith("/" + resourceId + "/" + EndpointPaths.TEXT + "/" + EndpointPaths.TEXTVIEWS);

    // now, retrieve the base layer definition
    RestResult<TextView> result3 = client.getTextViewDefinition(resourceId);
    assertRequestSucceeded(result3);
    TextView returnedDefinition = result3.get();
    softly.assertThat(returnedDefinition.getIgnoredElements()).as("SubresourceElements").containsExactly("note");
    softly.assertThat(returnedDefinition.getIncludedElementDefinitions()).as("BaseElements").containsExactly(baseElement1, baseElement2);
  }

}
