package nl.knaw.huygens.alexandria.util;

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