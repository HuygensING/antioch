package nl.knaw.huygens.alexandria.api.model.text.view;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.google.common.collect.Lists;
import nl.knaw.huygens.alexandria.api.JsonTypeNames;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

@JsonTypeName(JsonTypeNames.TEXTVIEWLIST)
public class TextViewList {
  @JsonIgnore
  List<TextViewDefinition> delegate = Lists.newArrayList();

  public int size() {
    return delegate.size();
  }

  @JsonIgnore
  public boolean isEmpty() {
    return delegate.isEmpty();
  }

  public boolean contains(Object o) {
    return delegate.contains(o);
  }

  public Iterator<TextViewDefinition> iterator() {
    return delegate.iterator();
  }

  public Object[] toArray() {
    return delegate.toArray();
  }

  public <T> T[] toArray(T[] a) {
    return delegate.toArray(a);
  }

  public boolean add(TextViewDefinition textViewDefinition) {
    return delegate.add(textViewDefinition);
  }

  public boolean remove(Object o) {
    return delegate.remove(o);
  }

  public boolean containsAll(Collection<?> c) {
    return delegate.containsAll(c);
  }

  public boolean addAll(Collection<? extends TextViewDefinition> c) {
    return delegate.addAll(c);
  }

  public boolean addAll(int index, Collection<? extends TextViewDefinition> c) {
    return delegate.addAll(index, c);
  }

  public boolean removeAll(Collection<?> c) {
    return delegate.removeAll(c);
  }

  public boolean retainAll(Collection<?> c) {
    return delegate.retainAll(c);
  }

  public void replaceAll(UnaryOperator<TextViewDefinition> operator) {
    delegate.replaceAll(operator);
  }

  public void sort(Comparator<? super TextViewDefinition> c) {
    delegate.sort(c);
  }

  public void clear() {
    delegate.clear();
  }

  public TextViewDefinition get(int index) {
    return delegate.get(index);
  }

  public TextViewDefinition set(int index, TextViewDefinition element) {
    return delegate.set(index, element);
  }

  public void add(int index, TextViewDefinition element) {
    delegate.add(index, element);
  }

  public TextViewDefinition remove(int index) {
    return delegate.remove(index);
  }

  public int indexOf(Object o) {
    return delegate.indexOf(o);
  }

  public int lastIndexOf(Object o) {
    return delegate.lastIndexOf(o);
  }

  public ListIterator<TextViewDefinition> listIterator() {
    return delegate.listIterator();
  }

  public ListIterator<TextViewDefinition> listIterator(int index) {
    return delegate.listIterator(index);
  }

  public List<TextViewDefinition> subList(int fromIndex, int toIndex) {
    return delegate.subList(fromIndex, toIndex);
  }

  public Spliterator<TextViewDefinition> spliterator() {
    return delegate.spliterator();
  }

  public boolean removeIf(Predicate<? super TextViewDefinition> filter) {
    return delegate.removeIf(filter);
  }

  public Stream<TextViewDefinition> stream() {
    return delegate.stream();
  }

  public Stream<TextViewDefinition> parallelStream() {
    return delegate.parallelStream();
  }

  public void forEach(Consumer<? super TextViewDefinition> action) {
    delegate.forEach(action);
  }
}
