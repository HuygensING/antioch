package nl.knaw.huygens.alexandria.storage.frames;

import java.util.List;

import peapod.annotations.Edge;
import peapod.annotations.In;

public abstract class AlexandriaVF {

  public abstract String getUuid();

  public abstract void setUuid(String uuidString);

  public abstract String getState();

  public abstract void setState(String state);

  public abstract String getProvenanceWho();

  public abstract void setProvenanceWho(String who);

  public abstract String getProvenanceWhen();

  public abstract void setProvenanceWhen(String epochSecond);

  public abstract String getProvenanceWhy();

  public abstract void setProvenanceWhy(String why);

  @In
  @Edge(AnnotationVF.ANNOTATES)
  public abstract List<AnnotationVF> getAnnotatedBy();
}
