package nl.knaw.huygens.alexandria.endpoint.command;

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
