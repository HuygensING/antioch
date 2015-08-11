package nl.knaw.huygens.alexandria.endpoint.search;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

import nl.knaw.huygens.alexandria.endpoint.JsonWrapperObject;
import nl.knaw.huygens.alexandria.endpoint.Prototype;

@JsonTypeName("query")
public class AlexandriaQuery extends JsonWrapperObject implements Prototype {
  // find: find(annotation|resource)
  String find = "annotation";

  // where: field.function(params)
  // functions: eq(par) - equals
  // match(par) - match regexp
  // inSet(par0,par1,...,parn) - equals in set
  // inRange(par0,par1) - par0 <= value <= par1
  String where = "";

  // sort: sort([-|+]field,...) - = descending, default = + = ascending
  String sort = "when";

  // return: return(field,...)
  @JsonProperty("return")
  String fields = "id";

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

}
