package nl.knaw.huygens.alexandria.client.model;

/*
 * #%L
 * alexandria-java-client
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

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.google.common.collect.Lists;

import nl.knaw.huygens.alexandria.api.JsonTypeNames;
import nl.knaw.huygens.alexandria.api.model.JsonWrapperObject;

@JsonTypeName(JsonTypeNames.ANNOTATIONLIST)
public class AnnotationList extends JsonWrapperObject implements List<AnnotationPojo> {
  final List<AnnotationPojo> delegate = Lists.newArrayList();

  // delegated methods

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
  public Iterator<AnnotationPojo> iterator() {
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
  public boolean add(AnnotationPojo e) {
    return delegate.add(e);
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
  public boolean addAll(Collection<? extends AnnotationPojo> c) {
    return delegate.addAll(c);
  }

  @Override
  public boolean addAll(int index, Collection<? extends AnnotationPojo> c) {
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
  public AnnotationPojo get(int index) {
    return delegate.get(index);
  }

  @Override
  public AnnotationPojo set(int index, AnnotationPojo element) {
    return delegate.set(index, element);
  }

  @Override
  public void add(int index, AnnotationPojo element) {
    delegate.add(index, element);
  }

  @Override
  public AnnotationPojo remove(int index) {
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
  public ListIterator<AnnotationPojo> listIterator() {
    return delegate.listIterator();
  }

  @Override
  public ListIterator<AnnotationPojo> listIterator(int index) {
    return delegate.listIterator(index);
  }

  @Override
  public List<AnnotationPojo> subList(int fromIndex, int toIndex) {
    return delegate.subList(fromIndex, toIndex);
  }

}
