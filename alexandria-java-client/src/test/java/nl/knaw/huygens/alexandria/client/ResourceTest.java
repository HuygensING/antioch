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
    String resourceRef = "corpus";
    ResourcePrototype resource = new ResourcePrototype(resourceRef);
    RestResult<UUID> result = client.addResource(resource);
    assertThat(result.hasFailed()).isFalse();
    UUID resourceUuid = result.get();
    Log.info("resourceUUID = {}", resourceUuid);
    assertThat(resourceUuid).isNotNull();

    // retrieve the resource
    RestResult<ResourceEntity> result2 = client.getResource(resourceUuid);
    assertThat(result2.hasFailed()).isFalse();
    ResourceEntity resourceEntity = result2.get();
    assertThat(resourceEntity).isNotNull();
    assertThat(resourceEntity.getRef()).isEqualTo(resourceRef);
    assertThat(resourceEntity.getState().getValue()).isEqualTo(AlexandriaState.TENTATIVE);

    // confirm the resource
    RestResult<Void> result3 = client.confirmResource(resourceUuid);
    assertThat(result3.hasFailed()).isFalse();

    // retrieve the resource again
    RestResult<ResourceEntity> result4 = client.getResource(resourceUuid);
    assertThat(result4.hasFailed()).isFalse();
    ResourceEntity resourceEntity2 = result4.get();
    assertThat(resourceEntity2).isNotNull();
    assertThat(resourceEntity2.getRef()).isEqualTo(resourceRef);
    assertThat(resourceEntity2.getState().getValue()).isEqualTo(AlexandriaState.CONFIRMED);
  }

  @Test
  public void testAddResourceWithProvenanceReturnsValidUUID() {
    client.setAuthKey(AUTHKEY);
    ResourcePrototype resource = new ResourcePrototype("corpus2").withProvenance(new ProvenancePrototype().setWho("test").setWhy("because test"));
    RestResult<UUID> result = client.addResource(resource);
    assertThat(result.hasFailed()).isFalse();
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
    assertThat(result.hasFailed()).isFalse();

    // retrieve the resource
    RestResult<ResourceEntity> result2 = client.getResource(resourceId);
    assertThat(result2.hasFailed()).isFalse();
    ResourceEntity resourceEntity = result2.get();
    assertThat(resourceEntity).isNotNull();
    assertThat(resourceEntity.getRef()).isEqualTo(ref);
    assertThat(resourceEntity.getState().getValue()).isEqualTo(AlexandriaState.CONFIRMED);
  }

}
