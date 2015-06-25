package nl.knaw.huygens.alexandria.model;

import java.util.UUID;

public class AlexandriaSubResource extends AbstractAnnotatable {
  private String sub;
  private AccountablePointer<AlexandriaResource> parentResourcePointer;

  public AlexandriaSubResource(UUID id, TentativeAlexandriaProvenance provenance) {
    super(id, provenance);
  }

  public String getSub() {
    return sub;
  }

  public void setSub(String sub) {
    this.sub = sub;
  }

  public AccountablePointer<AlexandriaResource> getParentResourcePointer() {
    return parentResourcePointer;
  }

  public void setParentResourcePointer(AccountablePointer<AlexandriaResource> parentResourcePointer) {
    this.parentResourcePointer = parentResourcePointer;
  }

}
