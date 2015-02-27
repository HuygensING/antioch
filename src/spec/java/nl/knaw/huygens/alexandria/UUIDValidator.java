package nl.knaw.huygens.alexandria;

import java.util.Optional;
import java.util.UUID;

public class UUIDValidator {
  private final String candidate;

  public static UUIDValidator of(String candidate) {
    return new UUIDValidator(candidate);
  }

  private UUIDValidator(String candidate) {
    this.candidate = candidate;
  }

  public Optional<String> whenValid(String s) {
    try {
      UUID.fromString(candidate);
      return Optional.of(s);
    } catch (IllegalArgumentException dulyNoted) {
      return Optional.empty();
    }
  }
}
