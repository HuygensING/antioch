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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import nl.knaw.huygens.antioch.api.model.JsonWrapperObject;

@JsonInclude(Include.NON_ABSENT)
public abstract class AbstractAccountablePojo<T> extends JsonWrapperObject {
  private ProvenancePojo provenance;

  @SuppressWarnings("unchecked")
  public T withProvenance(ProvenancePojo provenance) {
    this.provenance = provenance;
    return (T) this;
  }

  public ProvenancePojo getProvenance() {
    return provenance;
  }

}
