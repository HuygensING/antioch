package nl.knaw.huygens.antioch.client.model;

/*
 * #%L
 * antioch-java-client
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

import com.fasterxml.jackson.annotation.JsonTypeName;

import nl.knaw.huygens.antioch.api.JsonTypeNames;

@JsonTypeName(JsonTypeNames.ANNOTATION)
public class AnnotationPrototype extends Prototype {
  final AnnotationPojo delegate = new AnnotationPojo();

  public AnnotationPrototype setType(String type) {
    delegate.setType(type);
    return this;
  }

  public String getType() {
    return delegate.getType();
  }

  public AnnotationPrototype setValue(String value) {
    delegate.setValue(value);
    return this;
  }

  public String getValue() {
    return delegate.getValue();
  }

  public AnnotationPrototype setLocator(String locator) {
    delegate.setLocator(locator);
    return this;
  }

  public String getLocator() {
    return delegate.getLocator();
  }

  public AnnotationPrototype setProvenance(ProvenancePojo provenance) {
    delegate.withProvenance(provenance);
    return this;
  }

  public ProvenancePojo getProvenance() {
    return delegate.getProvenance();
  }

}
