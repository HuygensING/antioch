package nl.knaw.huygens.alexandria.api.model;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import com.google.common.collect.Lists;

public class AnnotatorList implements List<Annotator> {
  private List<Annotator> list = Lists.newArrayList();

  // @JsonUnwrapped
  // public List<Annotator> getList() {
  // return list;
  // }

  // delegated methods

  @Override
  public int size() {
    return list.size();
  }

  @Override
  public boolean isEmpty() {
    return list.isEmpty();
  }

  @Override
  public boolean contains(Object o) {
    return list.contains(o);
  }

  @Override
  public Iterator<Annotator> iterator() {
    return list.iterator();
  }

  @Override
  public Object[] toArray() {
    return list.toArray();
  }

  @Override
  public <T> T[] toArray(T[] a) {
    return list.toArray(a);
  }

  @Override
  public boolean add(Annotator e) {
    return list.add(e);
  }

  @Override
  public boolean remove(Object o) {
    return list.remove(o);
  }

  @Override
  public boolean containsAll(Collection<?> c) {
    return list.containsAll(c);
  }

  @Override
  public boolean addAll(Collection<? extends Annotator> c) {
    return list.addAll(c);
  }

  @Override
  public boolean addAll(int index, Collection<? extends Annotator> c) {
    return list.addAll(index, c);
  }

  @Override
  public boolean removeAll(Collection<?> c) {
    return list.removeAll(c);
  }

  @Override
  public boolean retainAll(Collection<?> c) {
    return list.retainAll(c);
  }

  @Override
  public void clear() {
    list.clear();
  }

  @Override
  public boolean equals(Object o) {
    return list.equals(o);
  }

  @Override
  public int hashCode() {
    return list.hashCode();
  }

  @Override
  public Annotator get(int index) {
    return list.get(index);
  }

  @Override
  public Annotator set(int index, Annotator element) {
    return list.set(index, element);
  }

  @Override
  public void add(int index, Annotator element) {
    list.add(index, element);
  }

  @Override
  public Annotator remove(int index) {
    return list.remove(index);
  }

  @Override
  public int indexOf(Object o) {
    return list.indexOf(o);
  }

  @Override
  public int lastIndexOf(Object o) {
    return list.lastIndexOf(o);
  }

  @Override
  public ListIterator<Annotator> listIterator() {
    return list.listIterator();
  }

  @Override
  public ListIterator<Annotator> listIterator(int index) {
    return list.listIterator(index);
  }

  @Override
  public List<Annotator> subList(int fromIndex, int toIndex) {
    return list.subList(fromIndex, toIndex);
  }

}
