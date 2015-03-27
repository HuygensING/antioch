package nl.knaw.huygens.alexandria;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

public interface AnnotationCreationRequest {
  String getType();

  Optional<String> getValue();

  Stream<UUID> streamAnnotations();

  Instant getCreatedOn();
}
