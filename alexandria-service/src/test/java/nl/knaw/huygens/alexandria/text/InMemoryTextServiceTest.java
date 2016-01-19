package nl.knaw.huygens.alexandria.text;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

public class InMemoryTextServiceTest {

  @Test
  public void whatGoesInMustComeOut() throws IOException {
    String content = "What goes in";
    InMemoryTextService imts = new InMemoryTextService();
    UUID resourceUUID = UUID.randomUUID();
    InputStream stream = IOUtils.toInputStream(content);
    imts.setFromStream(resourceUUID, stream);
    Optional<InputStream> optionalStream = imts.getAsStream(resourceUUID);
    assertThat(optionalStream).isPresent();
    String out = IOUtils.toString(optionalStream.get());
    assertThat(out).isEqualTo(content);
  }

}
