package nl.knaw.huygens.alexandria.util;

import java.util.Optional;
import java.util.UUID;

public class UUIDParser {
  private final String candidate;

  public static UUIDParser fromString(String candidate) {
    return new UUIDParser(candidate);
  }

  private UUIDParser(String candidate) {
    this.candidate = candidate;
  }

  public Optional<UUID> get() {
    try {
      return Optional.of(UUID.fromString(candidate));
    } catch (IllegalArgumentException dulyNoted) {
      return Optional.empty();
    }
  }
}
