package nl.knaw.huygens.alexandria.model;

import java.time.Instant;

public class AlexandriaProvenance {
  public static final String DEFAULT_WHY = "why";
  public static final String DEFAULT_WHO = "nederlab";

  private final String who;
  private final Accountable what;
  private final Instant when;
  private final String why;

  public AlexandriaProvenance(Accountable what, String who, Instant when, String why) {
    this.who = who;
    this.what = what;
    this.when = when;
    this.why = why;
  }

  public String getWho() {
    return who;
  }

  public Accountable getWhat() {
    return what;
  }

  public Instant getWhen() {
    return when;
  }

  public String getWhy() {
    return why;
  }
}
