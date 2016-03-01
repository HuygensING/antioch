package nl.knaw.huygens.alexandria.client;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class ProvenancePrototype {
  private String who;
  private String why;

  public String getWho() {
    return who;
  }

  public ProvenancePrototype setWho(String who) {
    this.who = who;
    return this;
  }

  public String getWhy() {
    return why;
  }

  public ProvenancePrototype setWhy(String why) {
    this.why = why;
    return this;
  }

}
