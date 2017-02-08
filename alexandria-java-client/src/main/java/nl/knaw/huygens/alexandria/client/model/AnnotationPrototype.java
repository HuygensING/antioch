package nl.knaw.huygens.alexandria.client.model;

/*
 * #%L
 * alexandria-java-client
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

import com.fasterxml.jackson.annotation.JsonTypeName;

import nl.knaw.huygens.alexandria.api.JsonTypeNames;

@JsonTypeName(JsonTypeNames.ANNOTATION)
public class AnnotationPrototype extends Prototype {
  AnnotationPojo delegate = new AnnotationPojo();

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
