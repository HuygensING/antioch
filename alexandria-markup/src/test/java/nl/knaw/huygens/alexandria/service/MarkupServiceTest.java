package nl.knaw.huygens.alexandria.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;

import org.junit.Test;

import com.google.common.collect.ImmutableMap;

import jersey.repackaged.com.google.common.collect.Lists;
import nl.knaw.huygens.alexandria.api.model.CommandResponse;
import nl.knaw.huygens.alexandria.api.model.text.view.ElementDefinition;
import nl.knaw.huygens.alexandria.api.model.text.view.TextView;
import nl.knaw.huygens.alexandria.api.model.text.view.TextViewDefinition;
import nl.knaw.huygens.alexandria.endpoint.command.WrapContentInElementCommand;
import nl.knaw.huygens.alexandria.model.AlexandriaProvenance;
import nl.knaw.huygens.alexandria.model.AlexandriaResource;
import nl.knaw.huygens.alexandria.model.TentativeAlexandriaProvenance;
import nl.knaw.huygens.alexandria.test.AlexandriaTest;
import nl.knaw.huygens.alexandria.textgraph.ParseResult;
import nl.knaw.huygens.alexandria.textgraph.TextGraphUtil;

public class MarkupServiceTest extends AlexandriaTest {
  MarkupService service = null;

  @Test
  public void testGetTextViewDefinitionsForResourceReturnsTheFirstDefinitionUpTheResourceChain() {
    AlexandriaResource resource = aResource();
    service.createOrUpdateResource(resource);
    ElementDefinition bedText = ElementDefinition.withName("text");
    ElementDefinition bedDiv = ElementDefinition.withName("div");
    List<ElementDefinition> list = Lists.newArrayList(bedText, bedDiv);
    TextView textView = new TextView();
    UUID resourceId = resource.getId();
    TextViewDefinition textViewDefinition = new TextViewDefinition();
    service.setTextView(resourceId, "baselayer", textView, textViewDefinition);

    UUID subUuid1 = UUID.randomUUID();
    String sub = "sub1";
    TentativeAlexandriaProvenance provenance = copyOf(resource.getProvenance());
    service.createSubResource(subUuid1, resourceId, sub, provenance);

    UUID subUuid2 = UUID.randomUUID();
    service.createSubResource(subUuid2, subUuid1, "sub2", provenance);

    List<TextView> views = service.getTextViewsForResource(subUuid2);
    assertThat(views).isNotEmpty();
    // List<ElementDefinition> returnedElementDefinitions = views.get(0).getIncludedElementDefinitions();
    // assertThat(returnedElementDefinitions).containsExactly(bedText, bedDiv);
  }

  @Test
  public void testGetTextViewDefinitionsForResourceReturnsNullOptionalsWhenNoDefinitionPresentUpTheResourceChain() {
    AlexandriaResource resource = aResource();
    service.createOrUpdateResource(resource);

    UUID resourceId = resource.getId();
    TentativeAlexandriaProvenance provenance = copyOf(resource.getProvenance());

    UUID subUuid1 = UUID.randomUUID();
    String sub = "sub1";
    service.createSubResource(subUuid1, resourceId, sub, provenance);

    UUID subUuid2 = UUID.randomUUID();
    service.createSubResource(subUuid2, subUuid1, "sub2", provenance);

    List<TextView> textViews = service.getTextViewsForResource(subUuid2);
    assertThat(textViews).isEmpty();
  }

  @Test
  public void testXmlInEqualsXmlOut() throws WebApplicationException, IOException {
    // given
    String xml = singleQuotesToDouble("<xml><p xml:id='p-1' a='A' z='Z' b='B'>Bla</p></xml>");
    UUID resourceId = aResourceUUIDWithXml(xml);

    // when
    String out = getResourceXml(resourceId);

    // then
    assertThat(out).isEqualTo(xml);
  }

  @Test
  public void testMilestoneHandling() throws WebApplicationException, IOException {
    // given
    String xml = singleQuotesToDouble("<text>\n"//
        + "<pb n='1' xml:id='pb-1'></pb>\n"//
        + "<p><figure><graphic url='beec002jour04ill02.gif'/></figure></p>\n"//
        + "</text>");
    String expectedXml = singleQuotesToDouble("<text>\n"//
        + "<pb n='1' xml:id='pb-1'/>\n"//
        + "<p><figure><graphic url='beec002jour04ill02.gif'/></figure></p>\n"//
        + "</text>");
    UUID resourceId = aResourceUUIDWithXml(xml);

    // when
    String out = getResourceXml(resourceId);

    // then
    assertThat(out).isEqualTo(expectedXml);
  }

  @Test
  public void testWrapContentInElementWorks() throws WebApplicationException, IOException {
    // given
    String xml = singleQuotesToDouble("<text>"//
        + "<div xml:id='div-1'><p xml:id='p-1'>Paragraph the First.</p></div>"//
        + "<div xml:id='div-2'><p xml:id='p-2'>Paragraph the Second.</p></div>"//
        + "</text>");
    String expected = singleQuotesToDouble("<text>"//
        + "<div xml:id='div-1'><hi rend='blue'><p xml:id='p-1'>Paragraph the First.</p></hi></div>"//
        + "<div xml:id='div-2'><p xml:id='p-2'><hi rend='blue'>Paragraph the Second.</hi></p></div>"//
        + "</text>");
    UUID resourceId = aResourceUUIDWithXml(xml);

    WrapContentInElementCommand command = new WrapContentInElementCommand(service);
    ImmutableMap<String, Serializable> elementMap = ImmutableMap.of(//
        "name", "hi", //
        "attributes", ImmutableMap.of("rend", "blue")//
    );
    Map<String, Object> parameterMap = ImmutableMap.<String, Object> builder()//
        .put("resourceIds", Lists.newArrayList(resourceId.toString()))//
        .put("xmlIds", Lists.newArrayList("div-1", "p-2"))//
        .put("element", elementMap)//
        .build();

    // when
    CommandResponse response = command.runWith(parameterMap);

    // then
    assertThat(response.getErrorLines()).isEmpty();
    assertThat(response.parametersAreValid()).isTrue();

    String out = getResourceXml(resourceId);
    assertThat(out).isEqualTo(expected);
  }

  private UUID aResourceUUIDWithXml(String xml) {
    AlexandriaResource resource = aResource();
    service.createOrUpdateResource(resource);

    UUID resourceId = resource.getId();
    ParseResult result = TextGraphUtil.parse(xml);
    service.storeTextGraph(resourceId, result);
    return resourceId;
  }

  private String getResourceXml(UUID resourceId) throws IOException {
    StreamingOutput streamXML = TextGraphUtil.streamingOutputXML(service, resourceId);
    OutputStream output = new ByteArrayOutputStream();
    streamXML.write(output);
    output.flush();
    return output.toString();
  }

  private TentativeAlexandriaProvenance copyOf(AlexandriaProvenance provenance) {
    return new TentativeAlexandriaProvenance(provenance.getWho(), provenance.getWhen(), provenance.getWhy());
  }

  private AlexandriaResource aResource() {
    UUID resourceId = UUID.randomUUID();
    TentativeAlexandriaProvenance provenance = new TentativeAlexandriaProvenance("who", Instant.now(), "why");
    return new AlexandriaResource(resourceId, provenance);
  }
}
