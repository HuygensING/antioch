package nl.knaw.huygens.alexandria.query;

/*
 * #%L
 * alexandria-service
 * =======
 * Copyright (C) 2015 - 2016 Huygens ING (KNAW)
 * =======
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class AlexandriaQueryParseException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  public AlexandriaQueryParseException(List<String> parseErrors) {
    super(buildMessage(parseErrors));
  }

  private static String buildMessage(List<String> parseErrors) {
    final StringBuilder builder = new StringBuilder("parse errors:\n");
    final AtomicInteger counter = new AtomicInteger(1);
    parseErrors.forEach(error -> builder.append(counter.getAndIncrement()).append(": ").append(error).append("\n"));
    return builder.toString();
  }

}
