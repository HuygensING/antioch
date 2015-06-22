package nl.knaw.huygens.alexandria.model;

import java.time.Instant;

public class TentativeAlexandriaProvenance {
  private final String who;
  private final Instant when;
  private final String why;

  public static TentativeAlexandriaProvenance createDefault() {
    return new TentativeAlexandriaProvenance(AlexandriaProvenance.DEFAULT_WHO, Instant.now(), AlexandriaProvenance.DEFAULT_WHY);
  }

  public TentativeAlexandriaProvenance(String who, Instant when, String why) {
    this.who = who;
    this.when = when;
    this.why = why;
  }

  public String getWho() {
    return who;
  }

  public Instant getWhen() {
    return when;
  }

  public String getWhy() {
    return why;
  }

  public AlexandriaProvenance bind(Accountable what) {
    return new AlexandriaProvenance(what, who, when, why);
  }

}
