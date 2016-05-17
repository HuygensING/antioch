package nl.knaw.huygens.alexandria.query;

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
