package nl.knaw.huygens.alexandria.client;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.util.UUID;

import org.junit.Test;

import nl.knaw.huygens.Log;
import nl.knaw.huygens.alexandria.api.EndpointPaths;
import nl.knaw.huygens.alexandria.api.model.AlexandriaState;
import nl.knaw.huygens.alexandria.api.model.BaseElementDefinition;
import nl.knaw.huygens.alexandria.api.model.BaseLayerDefinition;
import nl.knaw.huygens.alexandria.api.model.BaseLayerDefinitionPrototype;

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
  public void testSetAndRetrieveBaseLayerDefinition() {
    // first, create a resource
    client.setAuthKey(AUTHKEY);
    String ref = "corpus";
    ResourcePrototype resource = new ResourcePrototype(ref).withProvenance(new ProvenancePrototype().setWho("test").setWhy("because test"));
    UUID resourceId = UUID.randomUUID();
    RestResult<Void> result = client.setResource(resourceId, resource);
    assertRequestSucceeded(result);

    // then, set the base layer definition
    BaseElementDefinition baseElement1 = BaseElementDefinition.withName("body").withAttributes("lang");
    BaseElementDefinition baseElement2 = BaseElementDefinition.withName("p").withAttributes("n");
    BaseLayerDefinitionPrototype baselayerDefinition = new BaseLayerDefinitionPrototype().setBaseElements(baseElement1, baseElement2).setSubresourceElements("note");
    RestResult<URI> result2 = client.setBaseLayerDefinition(resourceId, baselayerDefinition);
    assertRequestSucceeded(result2);
    softly.assertThat(result2.get().toString()).as("Location").endsWith("/" + resourceId + "/" + EndpointPaths.BASELAYERDEFINITION);

    // now, retrieve the base layer definition
    RestResult<BaseLayerDefinition> result3 = client.getBaseLayerDefinition(resourceId);
    assertRequestSucceeded(result3);
    BaseLayerDefinition returnedDefinition = result3.get();
    softly.assertThat(returnedDefinition.getSubresourceElements()).as("SubresourceElements").containsExactly("note");
    softly.assertThat(returnedDefinition.getBaseElementDefinitions()).as("BaseElements").containsExactly(baseElement1, baseElement2);
  }

}
