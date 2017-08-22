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

import java.util.UUID;

import org.apache.commons.lang3.builder.CompareToBuilder;

import nl.knaw.huygens.alexandria.textlocator.AlexandriaTextLocator;

public class AlexandriaAnnotation extends AbstractAnnotatable implements Comparable<AlexandriaAnnotation> {

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

  @Override
  public int compareTo(AlexandriaAnnotation other) {
    return new CompareToBuilder()//
        .append(body, other.getBody())//
        .append(rev, other.getRevision())//
        .build();
  }

}
