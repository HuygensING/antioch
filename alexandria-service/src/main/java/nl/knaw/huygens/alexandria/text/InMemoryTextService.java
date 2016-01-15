package nl.knaw.huygens.alexandria.text;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class InMemoryTextService implements TextService {
  Map<UUID, String> texts = new HashMap<>();

  @Override
  public void set(UUID resourceUUID, String text) {
    texts.put(resourceUUID, text);
  }

  @Override
  public Optional<String> get(UUID resourceUUID) {
    return Optional.ofNullable(texts.get(resourceUUID));
  }

}
