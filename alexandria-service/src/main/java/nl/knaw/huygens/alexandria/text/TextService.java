package nl.knaw.huygens.alexandria.text;

import java.io.InputStream;
import java.util.Optional;
import java.util.UUID;

public interface TextService {
  // void set(UUID resourceUUID, String text);

  void setFromStream(UUID resourceUUID, InputStream stream);

  // Optional<String> get(UUID resourceUUID);

  Optional<InputStream> getAsStream(UUID resourceUUID);
}
