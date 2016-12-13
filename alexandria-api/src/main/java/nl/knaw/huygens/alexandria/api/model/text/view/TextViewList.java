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
public class TextViewList implements List<TextViewEntity> {
  @JsonIgnore
  List<TextViewEntity> delegate = Lists.newArrayList();

  @Override
  public int size() {
    return delegate.size();
  }

  @Override
  public boolean isEmpty() {
    return delegate.isEmpty();
  }

  @Override
  public boolean contains(Object o) {
    return delegate.contains(o);
  }

  @Override
  public Iterator<TextViewEntity> iterator() {
    return delegate.iterator();
  }

  @Override
  public Object[] toArray() {
    return delegate.toArray();
  }

  @Override
  public <T> T[] toArray(T[] a) {
    return delegate.toArray(a);
  }

  @Override
  public boolean add(TextViewEntity textViewEntity) {
    return delegate.add(textViewEntity);
  }

  @Override
  public boolean remove(Object o) {
    return delegate.remove(o);
  }

  @Override
  public boolean containsAll(Collection<?> c) {
    return delegate.containsAll(c);
  }

  @Override
  public boolean addAll(Collection<? extends TextViewEntity> c) {
    return delegate.addAll(c);
  }

  @Override
  public boolean addAll(int index, Collection<? extends TextViewEntity> c) {
    return delegate.addAll(index, c);
  }

  @Override
  public boolean removeAll(Collection<?> c) {
    return delegate.removeAll(c);
  }

  @Override
  public boolean retainAll(Collection<?> c) {
    return delegate.retainAll(c);
  }

  @Override
  public void replaceAll(UnaryOperator<TextViewEntity> operator) {
    delegate.replaceAll(operator);
  }

  @Override
  public void sort(Comparator<? super TextViewEntity> c) {
    delegate.sort(c);
  }

  @Override
  public void clear() {
    delegate.clear();
  }

  @Override
  public boolean equals(Object o) {
    return delegate.equals(o);
  }

  @Override
  public int hashCode() {
    return delegate.hashCode();
  }

  @Override
  public TextViewEntity get(int index) {
    return delegate.get(index);
  }

  @Override
  public TextViewEntity set(int index, TextViewEntity element) {
    return delegate.set(index, element);
  }

  @Override
  public void add(int index, TextViewEntity element) {
    delegate.add(index, element);
  }

  @Override
  public TextViewEntity remove(int index) {
    return delegate.remove(index);
  }

  @Override
  public int indexOf(Object o) {
    return delegate.indexOf(o);
  }

  @Override
  public int lastIndexOf(Object o) {
    return delegate.lastIndexOf(o);
  }

  @Override
  public ListIterator<TextViewEntity> listIterator() {
    return delegate.listIterator();
  }

  @Override
  public ListIterator<TextViewEntity> listIterator(int index) {
    return delegate.listIterator(index);
  }

  @Override
  public List<TextViewEntity> subList(int fromIndex, int toIndex) {
    return delegate.subList(fromIndex, toIndex);
  }

  @Override
  public Spliterator<TextViewEntity> spliterator() {
    return delegate.spliterator();
  }

  @Override
  public boolean removeIf(Predicate<? super TextViewEntity> filter) {
    return delegate.removeIf(filter);
  }

  @Override
  public Stream<TextViewEntity> stream() {
    return delegate.stream();
  }

  @Override
  public Stream<TextViewEntity> parallelStream() {
    return delegate.parallelStream();
  }

  @Override
  public void forEach(Consumer<? super TextViewEntity> action) {
    delegate.forEach(action);
  }
}
