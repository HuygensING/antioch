package nl.knaw.huygens.alexandria.client;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.Test;

import nl.knaw.huygens.Log;
import nl.knaw.huygens.alexandria.api.model.AlexandriaState;

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

}
