package nl.knaw.huygens.alexandria.storage.frames;

import peapod.annotations.Edge;
import peapod.annotations.Out;

public abstract class HasResourceVF implements VF {
  public static final String HAS_RESOURCE = "has_resource";

  @Out
  @Edge(HAS_RESOURCE)
  public abstract void setResource(ResourceVF resource);

  @Out
  @Edge(HAS_RESOURCE)
  public abstract ResourceVF getResource();

}
