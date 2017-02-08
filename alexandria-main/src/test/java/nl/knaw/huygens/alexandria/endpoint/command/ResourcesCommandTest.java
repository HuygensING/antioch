package nl.knaw.huygens.alexandria.endpoint.command;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.Test;

import nl.knaw.huygens.alexandria.api.model.CommandResponse;

public class ResourcesCommandTest {

  class TestCommand extends ResourcesCommand {

    @Override
    public String getName() {
      return null;
    }

    @Override
    public CommandResponse runWith(Map<String, Object> parameterMap) {
      return null;
    }
  }

  @Test
  public void testSplit() throws Exception {
    TestCommand command = new TestCommand();
    String uuidString = "3e8c6332-230c-4fc5-865f-0d51534f4375";
    List<ResourceViewId> resourceViewIds = command.split(uuidString + ":view-1,view-2");
    ResourceViewId rvi1 = new ResourceViewId(UUID.fromString(uuidString), "view-1");
    ResourceViewId rvi2 = new ResourceViewId(UUID.fromString(uuidString), "view-2");
    assertThat(resourceViewIds).containsExactly(rvi1, rvi2);
  }

}
