package nl.knaw.huygens.antioch.endpoint.annotation;

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

import java.util.Optional;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonTypeName;

import io.swagger.annotations.ApiModel;
import nl.knaw.huygens.antioch.api.JsonTypeNames;
import nl.knaw.huygens.antioch.endpoint.AbstractAccountablePrototype;

@JsonTypeName(JsonTypeNames.ANNOTATION)
@ApiModel(JsonTypeNames.ANNOTATION)
public class AnnotationPrototype extends AbstractAccountablePrototype {
  private String locator;
  private String type;

  @NotNull
  private String value;

  public Optional<String> getLocator() {
    return Optional.ofNullable(locator);
  }

  public Optional<String> getType() {
    return Optional.ofNullable(type);
  }

  public String getValue() {
    return value;
  }

}
