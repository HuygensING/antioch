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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public abstract class AbstractAnnotatable extends AbstractAccountable {

  // TODO: use AccountablePointers ?
  private final Set<AlexandriaAnnotation> annotations = new HashSet<>();

  protected AbstractAnnotatable(UUID id, TentativeAlexandriaProvenance provenance) {
    super(id, provenance);
  }

  public Set<AlexandriaAnnotation> getAnnotations() {
    return Collections.unmodifiableSet(annotations);
  }

  public void addAnnotation(AlexandriaAnnotation annotation) {
    annotations.add(annotation);
    @SuppressWarnings({ "unchecked", "rawtypes" })
    IdentifiablePointer<? extends Accountable> pointer = new IdentifiablePointer(this.getClass(), this.getId().toString());
    annotation.setAnnotatablePointer(pointer);
  }

}
