package nl.knaw.huygens.alexandria.endpoint.command;

/*
 * #%L
 * alexandria-main
 * =======
 * Copyright (C) 2015 - 2017 Huygens ING (KNAW)
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

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.tree.TerminalNode;

import nl.knaw.huygens.alexandria.antlr.AQL2BaseVisitor;
import nl.knaw.huygens.alexandria.antlr.AQL2Parser.ParameterContext;
import nl.knaw.huygens.alexandria.antlr.AQL2Parser.RootContext;

public class QueryVisitor extends AQL2BaseVisitor<Void> {
  public static final String QUOTE = String.valueOf('"');
  String function;
  List<Object> parameters = new ArrayList<>();

  @Override
  public Void visitRoot(RootContext ctx) {
    TerminalNode functionNode = ctx.FUNCTION();
    if (functionNode == null) {
      throw new RuntimeException("parse error: no function");
    }
    function = functionNode.getText();
    return super.visitRoot(ctx);
  }

  @Override
  public Void visitParameter(ParameterContext ctx) {
    parameters.add(parseParameterString(ctx.getText()));
    return super.visitParameter(ctx);
  }

  private static Object parseParameterString(String parameterString) {
    if (parameterString.startsWith(QUOTE) && parameterString.endsWith(QUOTE)) {
      return parameterString.replace(QUOTE, "");
    }
    return Long.valueOf(parameterString);
  }

  public String getFunction() {
    return function;
  }

  public List<Object> getParameters() {
    return parameters;
  }

}
