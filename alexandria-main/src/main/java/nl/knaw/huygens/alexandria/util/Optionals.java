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
