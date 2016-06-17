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

  @Test
  public void testAnnotationCommandWorks() {
    UUID resourceUuid = createResource("xml");
    String xml = "<root>"//
        + "<p xml:id=\"p-1\">"//
        + "<seg xml:id=\"seg-1\">Some</seg>"//
        + " text</p>"//
        + "</root>";
    // String expectedXml = "<root>"//
    // + "<p xml:id=\"p-1\"><annotation xml:lang=\"?\">"//
    // + "<seg xml:id=\"seg-1\">Some</seg>"//
    // + " text</annotation></p>"//
    // + "</root>";
    String expectedXml = "<root>"//
        + "<p xml:id=\"p-1\"><annotation xml:lang=\"?\">"//
        + "<seg xml:id=\"seg-1\"><annotation xml:lang=\"fr\">Some</annotation></seg>"//
        + " text</annotation></p>"//
        + "</root>";
    setResourceText(resourceUuid, xml);

    Map<String, String> annotations = ImmutableMap.of(//
        "seg-1", "fr", //
        "p-1", "?");

    for (Map.Entry<String, String> entry : annotations.entrySet()) {
      Map<String, Object> attributes = ImmutableMap.<String, Object> builder()//
          .put("xml:lang", entry.getValue())//
          .build();
      Map<String, Object> element = ImmutableMap.<String, Object> builder()//
          .put("name", "annotation")//
          .put("attributes", attributes)//
          .build();
      Map<String, Object> parameters = ImmutableMap.<String, Object> builder()//
          .put("resourceIds", ImmutableList.of(resourceUuid))//
          .put("xmlIds", ImmutableList.of(entry.getKey()))//
          .put("element", element)//
          .build();
      RestResult<CommandResponse> result = client.addCommand(Commands.WRAP_CONTENT_IN_ELEMENT, parameters);
      assertRequestSucceeded(result);
      CommandResponse commandResponse = result.get();
      assertThat(commandResponse.success()).isTrue();
    }

    RestResult<String> xmlReadResult = client.getTextAsString(resourceUuid);
    assertRequestSucceeded(xmlReadResult);
    String xml2 = xmlReadResult.get();
    assertThat(xml2).isEqualTo(expectedXml);
  }
}
