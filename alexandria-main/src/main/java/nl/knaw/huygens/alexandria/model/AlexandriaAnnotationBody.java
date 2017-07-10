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

public class AlexandriaAnnotationBody extends AbstractAccountable implements Comparable<AlexandriaAnnotationBody> {
  private final String type;
  private final String value;

  public AlexandriaAnnotationBody(UUID id, String type, String value, TentativeAlexandriaProvenance provenance) {
    super(id, provenance);
    this.type = type;
    this.value = value;
  }

  public String getType() {
    return type;
  }

  public String getValue() {
    return value;
  }

  @Override
  public int compareTo(AlexandriaAnnotationBody o) {
    return new CompareToBuilder()//
        .append(type, o.getType())//
        .append(value, o.getValue())//
        .build();
  }

}
