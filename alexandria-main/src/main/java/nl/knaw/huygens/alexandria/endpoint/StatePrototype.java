package nl.knaw.huygens.alexandria.endpoint;

import nl.knaw.huygens.alexandria.api.model.AlexandriaState;

public class StatePrototype {
  private AlexandriaState state;

  public AlexandriaState getState() {
    return state;
  }

  public void setState(AlexandriaState state) {
    this.state = state;
  }

  public boolean isConfirmed() {
    return AlexandriaState.CONFIRMED.equals(state);
  }
}
