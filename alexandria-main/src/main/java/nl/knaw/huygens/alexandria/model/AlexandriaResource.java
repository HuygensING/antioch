package nl.knaw.huygens.alexandria.model;

import java.util.UUID;

public class AlexandriaResource extends AbstractAnnotatable {

  private String ref;

  public AlexandriaResource(UUID id, TentativeAlexandriaProvenance provenance) {
    super(id, provenance);
  }

  public String getRef() {
    return ref;
  }

  public void setRef(String ref) {
    this.ref = ref;
  }

}
