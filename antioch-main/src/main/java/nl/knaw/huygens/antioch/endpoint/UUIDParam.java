package nl.knaw.huygens.antioch.endpoint;

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

public class UUIDParam extends AbstractParam<UUID> {
  public UUIDParam(String param) {
    super(param);
  }

  @Override
  protected UUID parse(String param) {
    return UUID.fromString(param);
  }

  @Override
  protected String getErrorMessage(String param, Throwable e) {
    return String.format("Malformed UUID: %s (%s)", param, e.getMessage());
  }
}
