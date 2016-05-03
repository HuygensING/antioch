package nl.knaw.huygens.alexandria.client;

import java.util.UUID;

import org.junit.Ignore;
import org.junit.Test;

import nl.knaw.huygens.Log;
import nl.knaw.huygens.alexandria.api.model.AlexandriaState;
import nl.knaw.huygens.alexandria.api.model.TextViewDefinition;

public class ResourceTextViewTest extends AlexandriaClientTest {
  @Ignore
  @Test
  public void testSettingResourceViewWorks() {
    client.setAuthKey(AUTHKEY);
    client.setAutoConfirm(false);
    String resourceRef = "corpus";
    ResourcePrototype resource = new ResourcePrototype(resourceRef);
    RestResult<UUID> result = client.addResource(resource);
    assertRequestSucceeded(result);
    UUID resourceUuid = result.get();
    Log.info("resourceUUID = {}", resourceUuid);
    softly.assertThat(resourceUuid).isNotNull();

    // confirm the resource
    RestResult<Void> result3 = client.confirmResource(resourceUuid);
    assertRequestSucceeded(result3);

    String textViewName = "view0";
    TextViewDefinition textView = new TextViewDefinition();
    // retrieve the resource again
    RestResult<ResourceEntity> result4 = client.setResourceTextView(resourceUuid, textViewName, textView);
    assertRequestSucceeded(result4);
    ResourceEntity resourceEntity2 = result4.get();
    softly.assertThat(resourceEntity2).isNotNull();
    softly.assertThat(resourceEntity2.getRef()).as("ref").isEqualTo(resourceRef);
    softly.assertThat(resourceEntity2.getState().getValue()).as("state").isEqualTo(AlexandriaState.CONFIRMED);
  }

}
