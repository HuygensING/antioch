package nl.knaw.huygens.antioch.endpoint.webannotation;

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

import java.util.UUID;

public class WebAnnotation {

  private final UUID uuid;
  private String json = "";
  private String eTag = "";

  public WebAnnotation(UUID id) {
    this.uuid = id;
  }

  public WebAnnotation setJson(String json) {
    this.json = json;
    return this;
  }

  public String json() {
    return json;
  }

  public WebAnnotation setETag(String eTag) {
    this.eTag = eTag;
    return this;
  }

  public String eTag() {
    return eTag;
  }

  public UUID getId() {
    return uuid;
  }



}
