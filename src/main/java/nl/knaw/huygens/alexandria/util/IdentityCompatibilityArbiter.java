package nl.knaw.huygens.alexandria.util;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

import nl.knaw.huygens.alexandria.exception.IdMismatchException;

public class IdentityCompatibilityArbiter {
  private final UUID uuid;

  private IdentityCompatibilityArbiter(UUID uuid) {
    this.uuid = uuid;
  }

  public static IdentityCompatibilityArbiter of(UUID uuid) {
    return new IdentityCompatibilityArbiter(uuid);
  }

  public IdentityCompatibilityArbiter requireCompatibleIdIn(Supplier<UUID> identifiable) {
    Optional.ofNullable(identifiable.get()).ifPresent(id -> {
      if (!id.equals(uuid)) {
        throw new IdMismatchException(uuid, id);
      }
    });
    return this;
  }
}
