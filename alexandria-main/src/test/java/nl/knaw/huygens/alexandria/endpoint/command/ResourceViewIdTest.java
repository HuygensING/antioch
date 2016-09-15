package nl.knaw.huygens.alexandria.endpoint.command;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class ResourceViewIdTest {

  @Test
  public void testFromStringWithViewName() throws Exception {
    String uuidString = "3e8c6332-230c-4fc5-865f-0d51534f4375";
    String viewName = "viewname";
    ResourceViewId rvi = ResourceViewId.fromString(uuidString + ":" + viewName);
    assertThat(rvi.getResourceId().toString()).isEqualTo(uuidString);
    assertThat(rvi.getTextViewName()).isPresent();
    assertThat(rvi.getTextViewName().get()).isEqualTo(viewName);
  }

  @Test
  public void testFromStringWithoutViewName() throws Exception {
    String uuidString = "3e8c6332-230c-4fc5-865f-0d51534f4376";
    ResourceViewId rvi = ResourceViewId.fromString(uuidString);
    assertThat(rvi.getResourceId().toString()).isEqualTo(uuidString);
    assertThat(rvi.getTextViewName()).isNotPresent();
  }

}
