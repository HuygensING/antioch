package nl.knaw.huygens.alexandria.model;

/*
 * #%L
 * alexandria-main
 * =======
 * Copyright (C) 2015 - 2017 Huygens ING (KNAW)
 * =======
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
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

  // public List<TextView> getDirectTextViews() {
  // return directTextViews;
  // }
  //
  // public void setDirectTextViews(List<TextView> textViews) {
  // this.directTextViews = textViews;
  // }

  @Override
  public int compareTo(AlexandriaResource other) {
    return cargo.compareToIgnoreCase(other.getCargo());
  }

}
