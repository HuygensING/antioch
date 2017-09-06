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

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonTypeName;

import nl.knaw.huygens.antioch.api.JsonTypeNames;

@JsonTypeName(JsonTypeNames.ANNOTATION)
@JsonInclude(Include.NON_ABSENT)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AnnotationPojo extends AbstractAccountablePojo<AnnotationPojo> {
  private String type;
  private String value;
  private String locator;

  public void setType(String type) {
    this.type = type;
  }

  public String getType() {
    return type;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  public void setLocator(String locator) {
    this.locator = locator;
  }

  public String getLocator() {
    return locator;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
  }

  @Override
  public boolean equals(Object other) {
    return EqualsBuilder.reflectionEquals(this, other);
  }

}
