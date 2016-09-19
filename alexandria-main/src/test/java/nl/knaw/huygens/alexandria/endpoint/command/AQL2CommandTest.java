package nl.knaw.huygens.alexandria.endpoint.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.util.Map;

import org.junit.Test;

import com.google.common.collect.ImmutableMap;

import nl.knaw.huygens.alexandria.api.model.CommandResponse;
import nl.knaw.huygens.alexandria.service.AlexandriaService;
import nl.knaw.huygens.alexandria.test.AlexandriaTest;

public class AQL2CommandTest extends AlexandriaTest {

  @Test
  public void testRunWith() throws Exception {
    AlexandriaService service = mock(AlexandriaService.class);
    AQL2Command ac = new AQL2Command(service);
    Map<String, Object> parameterMap = ImmutableMap.of(AQL2Command.COMMAND_PARAMETER, "hello(\"World\",\"You\")");
    CommandResponse response = ac.runWith(parameterMap);
    assertThat(response.getResult()).isEqualTo("Hello and welcome, World!\nHello and welcome, You!");
  }

}
