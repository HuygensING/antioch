package nl.knaw.huygens.alexandria.text;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.io.IOUtils;

public class InMemoryTextService implements TextService {
  Map<UUID, String> texts = new HashMap<>();

  // @Override
  // public void set(UUID resourceUUID, String text) {
  // texts.put(resourceUUID, text);
  // }

  @Override
  public void setFromStream(UUID resourceUUID, InputStream stream) {
    try {
      texts.put(resourceUUID, IOUtils.toString(stream));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  // @Override
  // public Optional<String> get(UUID resourceUUID) {
  // return Optional.ofNullable(texts.get(resourceUUID));
  // }

  @Override
  public Optional<InputStream> getAsStream(UUID resourceUUID) {
    if (texts.containsKey(resourceUUID)) {
      try {
        InputStream in = IOUtils.toInputStream(texts.get(resourceUUID), "UTF-8");
        return Optional.of(in);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
    return Optional.empty();
  }

}
