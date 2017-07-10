package nl.knaw.huygens.alexandria.api.model.search;

/*
 * #%L
 * alexandria-api
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

import javax.validation.constraints.Min;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

import nl.knaw.huygens.alexandria.api.JsonTypeNames;
import nl.knaw.huygens.alexandria.api.model.AlexandriaState;
import nl.knaw.huygens.alexandria.api.model.JsonWrapperObject;
import nl.knaw.huygens.alexandria.api.model.Prototype;

@JsonTypeName(JsonTypeNames.QUERY)
public class AlexandriaQuery extends JsonWrapperObject implements Prototype {
  // find: find(annotation|resource)
  private String find = "annotation";

  // where: field.function(params)
  // functions: eq(par) - equals
  // match(par) - match regexp
  // inSet(par0,par1,...,parn) - equals in set
  // inRange(par0,par1) - par0 <= value <= par1
  private String where = "";

  // sort: sort([-|+]field,...) - = descending, default = + = ascending
  private String sort = "-" + QueryField.when.name();

  // return: return(field,...)
  @JsonProperty("return")
  private String fields = QueryField.id.name();

  private Boolean distinct = false;

  @Min(1)
  private int pageSize = 10;

  public String getFind() {
    return find;
  }

  public AlexandriaQuery setFind(String find) {
    this.find = find;
    return this;
  }

  public String getWhere() {
    return where;
  }

  public AlexandriaQuery setWhere(String where) {
    this.where = where;
    String state = QueryField.state.name();
    if (!(where.startsWith(state + ":") || where.contains(") " + state + ":"))) {
      this.where += " " + state + ":" + QueryFunction.eq.name() + "(\"" + AlexandriaState.CONFIRMED.name() + "\")";
    }
    return this;
  }

  public String getSort() {
    return sort;
  }

  public AlexandriaQuery setSort(String sort) {
    this.sort = sort;
    return this;
  }

  public String getFields() {
    return fields;
  }

  public AlexandriaQuery setReturns(String fields) {
    this.fields = fields;
    return this;
  }

  public Boolean isDistinct() {
    return distinct;
  }

  public AlexandriaQuery setDistinct(Boolean distinct) {
    this.distinct = distinct;
    return this;
  }

  public int getPageSize() {
    return pageSize;
  }

  public AlexandriaQuery setPageSize(Integer pageSize) {
    this.pageSize = pageSize;
    return this;
  }

}
