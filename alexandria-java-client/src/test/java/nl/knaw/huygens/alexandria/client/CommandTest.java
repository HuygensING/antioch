package nl.knaw.huygens.alexandria.client;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import nl.knaw.huygens.alexandria.api.model.CommandResponse;
import nl.knaw.huygens.alexandria.api.model.Commands;

public class CommandTest extends AlexandriaClientTest {

  @Before
  public void before() {
    client.setAuthKey(AUTHKEY);
    client.setAutoConfirm(true);
  }

  @Test
  public void testAddUniqueIdCommmandWorks() {
    UUID resourceUuid = createResource("xml");
    String xml = "<text><p>Alinea 1</p><p>Alinea 2</p></text>";
    String expectedXml = singleQuotesToDouble("<text xml:id='text-1'><p xml:id='p-1'>Alinea 1</p><p xml:id='p-2'>Alinea 2</p></text>");
    setResourceText(resourceUuid, xml);
    Map<String, Object> parameters = ImmutableMap.<String, Object> builder()//
        .put("resourceIds", ImmutableList.of(resourceUuid))//
        .put("elements", ImmutableList.of("text", "p"))//
        .build();
    RestResult<CommandResponse> result = client.addCommand(Commands.ADD_UNIQUE_ID, parameters);
    assertRequestSucceeded(result);
    CommandResponse commandResponse = result.get();
    assertThat(commandResponse.success()).isTrue();

    RestResult<String> xmlReadResult = client.getTextAsString(resourceUuid);
    assertRequestSucceeded(xmlReadResult);
    String xml2 = xmlReadResult.get();
    assertThat(xml2).isEqualTo(expectedXml);
  }

}
