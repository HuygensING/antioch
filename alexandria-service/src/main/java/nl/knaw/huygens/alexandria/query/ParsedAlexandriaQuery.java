package nl.knaw.huygens.alexandria.query;

import static java.util.stream.Collectors.joining;

/*
 * #%L
 * alexandria-service
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

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import nl.knaw.huygens.alexandria.storage.Storage;
import nl.knaw.huygens.alexandria.storage.frames.AlexandriaVF;
import nl.knaw.huygens.alexandria.storage.frames.AnnotationVF;
import nl.knaw.huygens.alexandria.util.StreamUtil;

public class ParsedAlexandriaQuery {
  // this is just a container class for the results of processing the AlexandriaQuery parameters
  private static final Function<Storage, Stream<AnnotationVF>> DEFAULT_ANNOTATIONVF_FINDER = storage -> StreamUtil.stream(storage.find(AnnotationVF.class));

  private Class<? extends AlexandriaVF> vfClazz;
  private Boolean distinct;

  private List<String> returnFields;
  private List<String> fieldsToGroup = Lists.newArrayList();
  private Predicate<AnnotationVF> predicate;
  private Comparator<AnnotationVF> comparator;
  private Function<AnnotationVF, Map<String, Object>> mapper;
  private Function<Storage, Stream<AnnotationVF>> annotationVFFinder = DEFAULT_ANNOTATIONVF_FINDER;

  public ParsedAlexandriaQuery setVFClass(Class<? extends AlexandriaVF> vfClass) {
    this.vfClazz = vfClass;
    return this;
  }

  public Class<? extends AlexandriaVF> getVFClass() {
    return this.vfClazz;
  }

  public void setDistinct(Boolean distinct) {
    this.distinct = distinct;
  }

  public Boolean isDistinct() {
    return distinct;
  }

  public void setReturnFields(List<String> returnFields) {
    this.returnFields = returnFields;
  }

  public List<String> getReturnFields() {
    return returnFields;
  }

  public void setResultMapper(Function<AnnotationVF, Map<String, Object>> mapper) {
    this.mapper = mapper;
  }

  public Function<AnnotationVF, Map<String, Object>> getResultMapper() {
    return mapper;
  }

  public ParsedAlexandriaQuery setPredicate(Predicate<AnnotationVF> predicate) {
    this.predicate = predicate;
    return this;
  }

  public Predicate<AnnotationVF> getPredicate() {
    return predicate;
  }

  public ParsedAlexandriaQuery setResultComparator(Comparator<AnnotationVF> comparator) {
    this.comparator = comparator;
    return this;
  }

  public Comparator<AnnotationVF> getResultComparator() {
    return comparator;
  }

  public void setAnnotationVFFinder(Function<Storage, Stream<AnnotationVF>> annotationVFFinder) {
    this.annotationVFFinder = annotationVFFinder;
  }

  public Function<Storage, Stream<AnnotationVF>> getAnnotationVFFinder() {
    return annotationVFFinder;
  }

  public void setFieldsToGroup(List<String> listFields) {
    this.fieldsToGroup = listFields;
  }

  List<String> getFieldsToGroup() {
    return fieldsToGroup;
  }

  public boolean doGrouping() {
    return !fieldsToGroup.isEmpty();
  }

  public String concatenateGroupByFieldsValues(Map<String, Object> map) {
    if (doGrouping()) {
      return returnFields.stream()//
          .filter(f -> !fieldsToGroup.contains(f))//
          .sorted()//
          .map(map::get)//
          .map(Object::toString)//
          .collect(joining());
    }
    return null;
  }

  public Map<String, Object> collectListFieldValues(List<Map<String, Object>> mapList) {
    if (doGrouping()) {
      Map<String, Object> map = mapList.get(0);
      List<Map<String, Object>> groupedValuesList = Lists.newArrayList();
      for (Map<String, Object> resultMap : mapList) {
        Map<String, Object> groupedValuesMap = Maps.newHashMap();
        for (String field : fieldsToGroup) {
          groupedValuesMap.put(field, resultMap.get(field));
        }
        groupedValuesList.add(groupedValuesMap);
      }
      fieldsToGroup.forEach(map::remove);
      map.put("_list", groupedValuesList);
      return map;
    }
    return null;
  }
}
