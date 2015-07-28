package nl.knaw.huygens.alexandria.util;

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
