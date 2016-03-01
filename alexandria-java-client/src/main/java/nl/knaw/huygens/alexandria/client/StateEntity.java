package nl.knaw.huygens.alexandria.client;

import java.time.LocalDate;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;

import nl.knaw.huygens.alexandria.api.model.AlexandriaState;

public class StateEntity {
  AlexandriaState value;
  LocalDate since;

  public AlexandriaState getValue() {
    return value;
  }

  public void setValue(AlexandriaState value) {
    this.value = value;
  }

  public LocalDate getSince() {
    return since;
  }

  @JsonDeserialize(using = LocalDateDeserializer.class)
  public void setSince(LocalDate since) {
    this.since = since;
  }

}
