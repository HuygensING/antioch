package nl.knaw.huygens.alexandria.storage.frames;

import nl.knaw.huygens.alexandria.model.AlexandriaState;

public abstract class AlexandriaVF {

  public abstract String getUuid();

  public abstract void setUuid(String uuidString);

  public abstract String getState();

  public abstract void setState(String state);

  public abstract Long getStateSince();

  public abstract void setStateSince(Long epochSecond);

  public abstract String getProvenanceWho();

  public abstract void setProvenanceWho(String who);

  public abstract String getProvenanceWhen();

  public abstract void setProvenanceWhen(String epochSecond);

  public abstract String getProvenanceWhy();

  public abstract void setProvenanceWhy(String why);

  public boolean isDeprecated() {
    return AlexandriaState.DEPRECATED.equals(getState());
  }

}
