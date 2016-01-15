package nl.knaw.huygens.alexandria.text;

import java.util.Optional;
import java.util.UUID;

public interface TextService {
  void set(UUID resourceUUID, String text);
  Optional<String> get(UUID resourceUUID);
}
