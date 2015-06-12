package nl.knaw.huygens.alexandria.storage.frames;

import java.time.Instant;

import nl.knaw.huygens.alexandria.model.AlexandriaState;
import nl.knaw.huygens.alexandria.service.Labels;
import peapod.annotations.Vertex;

@Vertex(Labels.RESOURCE)
public abstract class AlexandriaVF {

  public abstract String getId();

  public abstract void setId(String id);

  public abstract AlexandriaState getState();

  public abstract void setState(AlexandriaState state);

  public abstract String getProvenanceWho();

  public abstract void setProvenanceWho(String who);

  public abstract Instant getProvenanceWhen();

  public abstract void setProvenanceWhen(Instant when);

  public abstract String getProvenanceWhy();

  public abstract void setProvenanceWhy(String why);

}
