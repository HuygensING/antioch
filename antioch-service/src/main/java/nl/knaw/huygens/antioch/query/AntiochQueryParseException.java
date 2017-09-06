package nl.knaw.huygens.antioch.query;

/*
 * #%L
 * antioch-service
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

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class AntiochQueryParseException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  public AntiochQueryParseException(List<String> parseErrors) {
    super(buildMessage(parseErrors));
  }

  private static String buildMessage(List<String> parseErrors) {
    final StringBuilder builder = new StringBuilder("parse errors:\n");
    final AtomicInteger counter = new AtomicInteger(1);
    parseErrors.forEach(error -> builder.append(counter.getAndIncrement()).append(": ").append(error).append("\n"));
    return builder.toString();
  }

}
