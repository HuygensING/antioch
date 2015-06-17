package nl.knaw.huygens.alexandria.endpoint;

import java.time.Instant;
import java.util.Optional;

import nl.knaw.huygens.alexandria.model.AlexandriaProvenance;
import nl.knaw.huygens.alexandria.model.TentativeAlexandriaProvenance;

public class ProvenancePrototype {
  private String who;
  private InstantParam when;
  private String why;

  public Optional<String> getWho() {
    return Optional.ofNullable(who);
  }

  public Instant getWhen() {
    return when == null ? Instant.now() : when.getValue();
  }

  public Optional<String> getWhy() {
    return Optional.ofNullable(why);
  }

  public TentativeAlexandriaProvenance getValue() {
    return new TentativeAlexandriaProvenance(//
        getWho().orElse(AlexandriaProvenance.DEFAULT_WHO),//
        getWhen(),//
        getWhy().orElse(AlexandriaProvenance.DEFAULT_WHY)//
    );
  }
}
