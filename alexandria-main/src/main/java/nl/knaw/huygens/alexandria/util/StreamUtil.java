package nl.knaw.huygens.alexandria.util;

/*
 * #%L
 * alexandria-main
 * =======
 * Copyright (C) 2015 - 2017 Huygens ING (KNAW)
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
    private NodeList nodelist;
    private int size;
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
