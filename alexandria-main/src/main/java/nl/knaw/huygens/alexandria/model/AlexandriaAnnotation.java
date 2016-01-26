package nl.knaw.huygens.alexandria.model;

/*
 * #%L
 * alexandria-main
 * =======
 * Copyright (C) 2015 - 2016 Huygens ING (KNAW)
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

import java.util.UUID;

import nl.knaw.huygens.alexandria.textlocator.AlexandriaTextLocator;

public class AlexandriaAnnotation extends AbstractAnnotatable {

  private final AlexandriaAnnotationBody body;
  private IdentifiablePointer<?> annotatedPointer;
  private Integer rev = 0;
  private AlexandriaTextLocator locator;

  public AlexandriaAnnotation(UUID id, AlexandriaAnnotationBody body, TentativeAlexandriaProvenance provenance) {
    super(id, provenance);
    this.body = body;
  }

  public AlexandriaAnnotationBody getBody() {
    return body;
  }

  public void setAnnotatablePointer(IdentifiablePointer<?> pointer) {
    this.annotatedPointer = pointer;
  }

  public IdentifiablePointer<?> getAnnotatablePointer() {
    return annotatedPointer;
  }

  public Integer getRevision() {
    return rev;
  }

  public void setRevision(Integer rev) {
    this.rev = rev;
  }

  public AlexandriaTextLocator getLocator() {
    return locator;
  }

  public void setLocator(AlexandriaTextLocator locator) {
    this.locator = locator;
  }

}
