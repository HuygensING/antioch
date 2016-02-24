package nl.knaw.huygens.alexandria.model.search;

/*
 * #%L
 * alexandria-main
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

import javax.validation.constraints.Min;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

import nl.knaw.huygens.alexandria.api.model.JsonWrapperObject;
import nl.knaw.huygens.alexandria.endpoint.Prototype;
import nl.knaw.huygens.alexandria.model.AlexandriaState;

@JsonTypeName("query")
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

  public void setFind(String find) {
    this.find = find;
  }

  public String getWhere() {
    return where;
  }

  public void setWhere(String where) {
    this.where = where;
    String state = QueryField.state.name();
    if (!(where.startsWith(state + ":") || where.contains(") " + state + ":"))) {
      this.where += " " + state + ":" + QueryFunction.eq.name() + "(\"" + AlexandriaState.CONFIRMED.name() + "\")";
    }
  }

  public String getSort() {
    return sort;
  }

  public void setSort(String sort) {
    this.sort = sort;
  }

  public String getFields() {
    return fields;
  }

  public void setFields(String fields) {
    this.fields = fields;
  }

  public Boolean isDistinct() {
    return distinct;
  }

  public void setDistinct(Boolean distinct) {
    this.distinct = distinct;
  }

  public int getPageSize() {
    return pageSize;
  }

  public AlexandriaQuery setPageSize(Integer pageSize) {
    this.pageSize = pageSize;
    return this;
  }

}
