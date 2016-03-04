package nl.knaw.huygens.alexandria.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class StatePrototype {
  private AlexandriaState state;

  public AlexandriaState getState() {
    return state;
  }

  public StatePrototype setState(AlexandriaState state) {
    this.state = state;
    return this;
  }

  @JsonIgnore
  public boolean isConfirmed() {
    return AlexandriaState.CONFIRMED.equals(state);
  }
}
