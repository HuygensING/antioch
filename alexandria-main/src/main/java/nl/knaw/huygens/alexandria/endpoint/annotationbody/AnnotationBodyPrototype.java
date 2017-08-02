package nl.knaw.huygens.alexandria.endpoint.annotationbody;

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

import java.util.Optional;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonTypeName;

import nl.knaw.huygens.alexandria.api.JsonTypeNames;
import nl.knaw.huygens.alexandria.api.model.AlexandriaState;
import nl.knaw.huygens.alexandria.api.model.JsonWrapperObject;
import nl.knaw.huygens.alexandria.api.model.Prototype;
import nl.knaw.huygens.alexandria.endpoint.UUIDParam;

@JsonTypeName(JsonTypeNames.ANNOTATIONBODY)
public class AnnotationBodyPrototype extends JsonWrapperObject implements Prototype {
  @NotNull
  private UUIDParam id;

  private String type;

  @NotNull
  private String value;

  private AlexandriaState state;

  public UUIDParam getId() {
    return id;
  }

  public Optional<String> getType() {
    return Optional.ofNullable(type);
  }

  public String getValue() {
    return value;
  }

  public AlexandriaState getState() {
    return state;
  }

}
