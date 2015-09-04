package nl.knaw.huygens.alexandria.query;

import java.util.List;

import com.google.common.collect.Lists;

class WhereToken {
  QueryField property;
  QueryFunction function;
  List<Object> parameters = Lists.newArrayList();

  public WhereToken(QueryField property, QueryFunction function, List<Object> parameters) {
    this.property = property;
    this.function = function;
    this.parameters = parameters;
  }

  public QueryField getProperty() {
    return property;
  }

  public QueryFunction getFunction() {
    return function;
  }

  public List<Object> getParameters() {
    return parameters;
  }

  public boolean hasResourceProperty() {
    return QueryField.RESOURCE_FIELDS.contains(getProperty());
  }

}