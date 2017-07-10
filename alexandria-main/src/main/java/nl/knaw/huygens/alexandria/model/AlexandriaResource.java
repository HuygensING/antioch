package nl.knaw.huygens.alexandria.model;

/*
 * #%L
 * alexandria-main
 * =======
 * Copyright (C) 2015 - 2017 Huygens ING (KNAW)
 * =======
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

import jersey.repackaged.com.google.common.collect.Lists;

public class AlexandriaResource extends AbstractAnnotatable implements Comparable<AlexandriaResource> {
  private String cargo; // ref for resource, sub for subresource
  private Optional<IdentifiablePointer<AlexandriaResource>> parentResourcePointer = Optional.empty(); // only used in subresources
  private Collection<IdentifiablePointer<AlexandriaResource>> subResourcePointers = Lists.newArrayList();
  // private List<TextView> directTextViews = new ArrayList<>();
  private boolean hasText = false;

  public AlexandriaResource(UUID id, TentativeAlexandriaProvenance provenance) {
    super(id, provenance);
  }

  public String getCargo() {
    return cargo;
  }

  public void setCargo(String cargo) {
    this.cargo = cargo;
  }

  public Optional<IdentifiablePointer<AlexandriaResource>> getParentResourcePointer() {
    return parentResourcePointer;
  }

  public void setParentResourcePointer(IdentifiablePointer<AlexandriaResource> parentResourcePointer) {
    this.parentResourcePointer = Optional.of(parentResourcePointer);
  }

  public Collection<IdentifiablePointer<AlexandriaResource>> getSubResourcePointers() {
    return subResourcePointers;
  }

  public void addSubResourcePointer(IdentifiablePointer<AlexandriaResource> pointer) {
    subResourcePointers.add(pointer);
  }

  public boolean isSubResource() {
    return parentResourcePointer.isPresent();
  }

  public boolean hasText() {
    return hasText;
  }

  public void setHasText(Boolean _hasText) {
    if (_hasText != null) {
      hasText = _hasText;
    }
  }

  @Override
  public int compareTo(AlexandriaResource other) {
    return cargo.compareToIgnoreCase(other.getCargo());
  }

}
