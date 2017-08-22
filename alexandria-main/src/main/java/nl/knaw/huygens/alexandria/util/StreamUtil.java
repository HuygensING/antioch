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

import java.util.Iterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class StreamUtil {
  public static <S> Stream<S> stream(Iterator<S> input) {
    return stream(input, false);
  }

  public static <S> Stream<S> parallelStream(Iterator<S> input) {
    return stream(input, true);
  }

  private static <S> Stream<S> stream(Iterator<S> input, boolean parallel) {
    Iterable<S> it = () -> input;
    return StreamSupport.stream(it.spliterator(), parallel);
  }

  public static <S> Stream<S> stream(Iterable<S> input) {
    return stream(input, false);
  }

  public static <S> Stream<S> parallelStream(Iterable<S> input) {
    return stream(input, true);
  }

  private static <S> Stream<S> stream(Iterable<S> input, boolean parallel) {
    return StreamSupport.stream(input.spliterator(), parallel);
  }

  public static class NodeIterator implements Iterator<Node> {
    private final NodeList nodelist;
    private final int size;
    private int i;

    NodeIterator(NodeList nodelist) {
      this.nodelist = nodelist;
      this.size = nodelist.getLength();
      this.i = 0;
    }

    @Override
    public boolean hasNext() {
      return i < size;
    }

    @Override
    public Node next() {
      return nodelist.item(i++);
    }
  }

  public static Stream<Node> stream(NodeList nodelist) {
    NodeIterator nodeIterator = new NodeIterator(nodelist);
    return stream(nodeIterator);
  }

}
