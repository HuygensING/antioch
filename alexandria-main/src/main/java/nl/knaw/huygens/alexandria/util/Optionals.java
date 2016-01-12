package nl.knaw.huygens.alexandria.util;

/*
 * #%L
 * alexandria-main
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

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

// Thanks to Christoffer Sawicki (qerub) for https://gist.github.com/qerub/9299fce67ab74e114633
public class Optionals {
  private Optionals() {
  }

  public static void main(String[] args) {
    Optional<String> opt1 = Optional.of("1");
    Optional<String> opt2 = Optional.of("2");
    Optional<String> opt3 = Optional.empty();

    // Transform an optional into something else via a stream:
    Set<String> res1 = stream(opt1).collect(toSet());

    // Flatten a collection of optionals:
    List<String> res2 = Stream.of(opt1, opt2, opt3).flatMap(Optionals::stream).collect(toList());

    // Combining multiple optionals, keeping the first present value:
    Optional<String> res3 = or(opt1, opt2, opt3);
  }

  /*
   * This will be included in Java 9, but until then...
   * (https://bugs.openjdk.java.net/browse/JDK-8050820)
   */
  public static <T> Stream<T> stream(Optional<T> self) {
    return self.map(Stream::of).orElseGet(Stream::empty);
  }

  @SafeVarargs
  public static <T> Optional<T> or(Optional<T>... xs) {
    return Arrays.stream(xs).flatMap(Optionals::stream).findFirst();
  }
}
