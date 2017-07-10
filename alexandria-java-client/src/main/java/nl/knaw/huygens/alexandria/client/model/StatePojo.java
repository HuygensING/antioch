package nl.knaw.huygens.alexandria.client.model;

/*
 * #%L
 * alexandria-java-client
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

import java.time.LocalDate;

import org.apache.commons.lang3.builder.EqualsBuilder;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;

import nl.knaw.huygens.alexandria.api.model.AlexandriaState;

public class StatePojo {
  AlexandriaState value;
  LocalDate since;

  public AlexandriaState getValue() {
    return value;
  }

  public void setValue(AlexandriaState value) {
    this.value = value;
  }

  public LocalDate getSince() {
    return since;
  }

  @JsonDeserialize(using = LocalDateDeserializer.class)
  public void setSince(LocalDate since) {
    this.since = since;
  }

  @Override
  public boolean equals(Object other) {
    return EqualsBuilder.reflectionEquals(this, other);
  }
}
