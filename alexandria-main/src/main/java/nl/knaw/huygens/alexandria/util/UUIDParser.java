package nl.knaw.huygens.alexandria.util;

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
import java.util.UUID;

public class UUIDParser {
  private final String candidate;

  public static UUIDParser fromString(String candidate) {
    return new UUIDParser(candidate);
  }

  private UUIDParser(String candidate) {
    this.candidate = candidate;
  }

  public Optional<UUID> get() {
    try {
      return Optional.of(UUID.fromString(candidate));
    } catch (IllegalArgumentException dulyNoted) {
      return Optional.empty();
    }
  }
}
