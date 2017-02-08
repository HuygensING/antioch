package nl.knaw.huygens.alexandria.storage.frames;

public abstract class IdentifiableVF implements VF {

  public abstract String getUuid();

  public abstract void setUuid(String uuidString);

}
