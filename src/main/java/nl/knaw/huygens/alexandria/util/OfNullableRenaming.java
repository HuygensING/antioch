package nl.knaw.huygens.alexandria.util;

import java.util.Optional;

public class OfNullableRenaming {
  public static <T> Optional<T> ifPresent(T it) {
    return Optional.ofNullable(it);
  }
}
