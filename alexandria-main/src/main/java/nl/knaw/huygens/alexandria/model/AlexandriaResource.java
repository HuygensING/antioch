package nl.knaw.huygens.alexandria.model;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

import jersey.repackaged.com.google.common.collect.Lists;

public class AlexandriaResource extends AbstractAnnotatable {

  private String cargo; // ref for resource, sub for subresource
  private Optional<AccountablePointer<AlexandriaResource>> parentResourcePointer = Optional.empty(); // only used in subresources
  private Collection<AccountablePointer<AlexandriaResource>> subResourcePointers = Lists.newArrayList();

  public AlexandriaResource(UUID id, TentativeAlexandriaProvenance provenance) {
    super(id, provenance);
  }

  public String getCargo() {
    return cargo;
  }

  public void setCargo(String cargo) {
    this.cargo = cargo;
  }

  public Optional<AccountablePointer<AlexandriaResource>> getParentResourcePointer() {
    return parentResourcePointer;
  }

  public void setParentResourcePointer(AccountablePointer<AlexandriaResource> parentResourcePointer) {
    this.parentResourcePointer = Optional.of(parentResourcePointer);
  }

  public Collection<AccountablePointer<AlexandriaResource>> getSubResourcePointers() {
    return subResourcePointers;
  }

  public void addSubResourcePointer(AccountablePointer<AlexandriaResource> pointer) {
    subResourcePointers.add(pointer);
  }

  public boolean isSubResource() {
    return parentResourcePointer.isPresent();
  }

}
