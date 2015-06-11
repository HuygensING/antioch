package nl.knaw.huygens.alexandria.storage.frames;

import java.time.Instant;

import nl.knaw.huygens.alexandria.service.Labels;
import peapod.annotations.Vertex;

@Vertex(Labels.RESOURCE)
public abstract class AlexandriaResourceVF {
  public abstract String getRef();

  public abstract void setRef(String ref);

  public abstract String getId();

  public abstract void setId(String id);

  public abstract String getProvenanceWho();

  public abstract void setProvenanceWho(String who);

  public abstract Instant getProvenanceWhen();

  public abstract void setProvenanceWhen(Instant when);

  public abstract String getProvenanceWhy();

  public abstract void setProvenanceWhy(String why);

}
