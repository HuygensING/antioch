package nl.knaw.huygens.alexandria.query;

/*
 * #%L
 * alexandria-service
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

import static java.util.stream.Collectors.toList;

import java.util.List;

import com.google.common.collect.Lists;

import nl.knaw.huygens.alexandria.antlr.AQLBaseVisitor;
import nl.knaw.huygens.alexandria.antlr.AQLParser.ParameterContext;
import nl.knaw.huygens.alexandria.antlr.AQLParser.SubQueryContext;
import nl.knaw.huygens.alexandria.api.model.search.QueryField;
import nl.knaw.huygens.alexandria.api.model.search.QueryFunction;

public class QueryVisitor extends AQLBaseVisitor<Void> {
  public static final String QUOTE = String.valueOf('"');

  private List<WhereToken> whereTokens = Lists.newArrayList();

  @Override
  public Void visitSubQuery(SubQueryContext ctx) {
    List<Object> parameters = ctx.parameters().parameter().stream()//
        .map(ParameterContext::getText)//
        .map(QueryVisitor::parseParameterString)//
        .collect(toList());
    QueryFunction function = QueryFunction.valueOf(ctx.FUNCTION().getText());
    QueryField property = QueryField.fromExternalName(ctx.FIELD_NAME().getText());
    WhereToken wToken = new WhereToken(property, function, parameters);
    whereTokens.add(wToken);
    return null;
  }

  public List<WhereToken> getWhereTokens() {
    return whereTokens;
  }

  private static Object parseParameterString(String parameterString) {
    if (parameterString.startsWith(QUOTE) && parameterString.endsWith(QUOTE)) {
      return parameterString.replace(QUOTE, "");
    }
    return Long.valueOf(parameterString);
  }

}
