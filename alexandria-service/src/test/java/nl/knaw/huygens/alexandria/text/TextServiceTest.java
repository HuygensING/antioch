package nl.knaw.huygens.alexandria.text;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import java.util.UUID;

import org.junit.Test;

import nl.knaw.huygens.Log;
import nl.knaw.huygens.alexandria.endpoint.resource.text.TextQuery;
import nl.knaw.huygens.alexandria.endpoint.resource.text.TextQueryResult;

public class TextServiceTest implements TextService {

  private String xml = "";

  @Test
  public void executeQueryWithValidXpath() {
    xml = "<xml><text id=\"m\"><body>elle</body></text><text id=\"s\"><body>claudia</body></text></xml>";
    TextQuery textQuery = new TextQuery();
    textQuery.setQuery("//text[@id=\"m\"]/body");
    textQuery.setResourceUUID(UUID.randomUUID());
    textQuery.setType("xpath");
    TextQueryResult executeQuery = this.executeQuery(textQuery);
    Log.info("result={}", executeQuery);
    assertThat(executeQuery.hasErrors()).isFalse();
    assertThat(executeQuery.getResults()).hasSize(1);
    assertThat(executeQuery.getResults().get(0)).isEqualTo("<body>elle</body>");
  }

  @Test
  public void executeQueryWithInvalidXpath() {
    xml = "<xml><text id=\"m\"><body>elle</body></text><text id=\"s\"><body>claudia</body></text></xml>";
    TextQuery textQuery = new TextQuery();
    textQuery.setQuery("//");
    textQuery.setResourceUUID(UUID.randomUUID());
    textQuery.setType("xpath");
    TextQueryResult executeQuery = this.executeQuery(textQuery);
    Log.info("result={}", executeQuery);
    assertThat(executeQuery.hasErrors()).isTrue();
    assertThat(executeQuery.getResults()).hasSize(0);
    assertThat(executeQuery.getErrors().get(0)).isEqualTo("javax.xml.transform.TransformerException: A location step was expected following the '/' or '//' token.");
  }

  @Test
  public void executeQueryWithTypeOtherThatXPath() {
    xml = "<xml></xml>";
    TextQuery textQuery = new TextQuery();
    textQuery.setQuery("//text[@id=\"m\"]/head");
    textQuery.setResourceUUID(UUID.randomUUID());
    textQuery.setType("xquery");
    TextQueryResult executeQuery = this.executeQuery(textQuery);
    Log.info("result={}", executeQuery);
    assertThat(executeQuery.hasErrors()).isTrue();
    assertThat(executeQuery.getResults()).hasSize(0);
    assertThat(executeQuery.getErrors().get(0)).isEqualTo("type not recognized:  xquery");
  }

  @Override
  public void set(UUID resourceUUID, String text) {
  }

  @Override
  public Optional<String> get(UUID resourceUUID) {
    return Optional.of(xml);
  }
}
