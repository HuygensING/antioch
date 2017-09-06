package nl.knaw.huygens.antioch.query;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import nl.knaw.huygens.antioch.storage.Storage;
import nl.knaw.huygens.antioch.storage.frames.AntiochVF;
import nl.knaw.huygens.antioch.storage.frames.AnnotationVF;
import nl.knaw.huygens.antioch.util.StreamUtil;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;

/*
 * #%L
 * antioch-service
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

public class ParsedAntiochQuery {

  enum ListSizeSortOrder {
    none, ascending, descending
  }

  // this is just a container class for the results of processing the AntiochQuery parameters
  private static final Function<Storage, Stream<AnnotationVF>> DEFAULT_ANNOTATIONVF_FINDER = storage -> StreamUtil.stream(storage.find(AnnotationVF.class));

  private Class<? extends AntiochVF> vfClazz;
  private Boolean distinct;

  private List<String> returnFields;
  private List<String> fieldsToGroup = Lists.newArrayList();
  private Predicate<AnnotationVF> predicate;
  private Comparator<AnnotationVF> comparator;
  private Function<AnnotationVF, Map<String, Object>> mapper;
  private Function<Storage, Stream<AnnotationVF>> annotationVFFinder = DEFAULT_ANNOTATIONVF_FINDER;

  private ListSizeSortOrder listSizeSortOrder = ListSizeSortOrder.none;

  private Function<Storage, Stream<Map<String, Object>>> resultStreamMapper;

  public ParsedAntiochQuery setVFClass(Class<? extends AntiochVF> vfClass) {
    this.vfClazz = vfClass;
    return this;
  }

  public Class<? extends AntiochVF> getVFClass() {
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

  public ParsedAntiochQuery setPredicate(Predicate<AnnotationVF> predicate) {
    this.predicate = predicate;
    return this;
  }

  public Predicate<AnnotationVF> getPredicate() {
    return predicate;
  }

  public ParsedAntiochQuery setResultComparator(Comparator<AnnotationVF> comparator) {
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

  public Function<Storage, Stream<Map<String, Object>>> getResultStreamMapper() {
    if (vfClazz == AnnotationVF.class) {
      return (storage) -> annotationVFFinder//
        .apply(storage)//
        .filter(predicate)//
        .sorted(comparator)//
        .map(mapper);
    }

    return resultStreamMapper;
  }

  public void setResultStreamMapper(Function<Storage, Stream<Map<String, Object>>> resultStreamMapper) {
    this.resultStreamMapper = resultStreamMapper;
  }

  public ParsedAntiochQuery setListSizeSortOrder(ListSizeSortOrder listSizeSortOrder) {
    this.listSizeSortOrder = listSizeSortOrder;
    return this;
  }

  public Comparator<Map<String, Object>> getListSizeComparator() {
    switch (listSizeSortOrder) {
      case ascending:
        return Comparator.comparing(this::getListSize);
      case descending:
        return Comparator.comparing(this::getListSize).reversed();
      default:
        return null;
    }
  }

  private int getListSize(Map<String, Object> resultMap) {
    return (Integer) resultMap.get(AntiochQueryParser.LIST_SIZE);
  }

  public boolean sortOnListSize() {
    return !ListSizeSortOrder.none.equals(listSizeSortOrder);
  }
}
