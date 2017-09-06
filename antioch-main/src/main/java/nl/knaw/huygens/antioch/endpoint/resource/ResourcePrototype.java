package nl.knaw.huygens.antioch.endpoint.resource;

/*
 * #%L
 * antioch-main
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

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonTypeName;

import nl.knaw.huygens.antioch.api.JsonTypeNames;
import nl.knaw.huygens.antioch.endpoint.AbstractAccountablePrototype;

@JsonTypeName(JsonTypeNames.RESOURCE)
public class ResourcePrototype extends AbstractAccountablePrototype {

  @NotNull(message = "{nl.knaw.huygens.antioch.endpoint.resource.ResourceProtoType.ref.NotNull.message}")
  private String ref;

  public String getRef() {
    return ref;
  }

}
