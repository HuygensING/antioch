package nl.knaw.huygens.alexandria.antlr;

import java.util.Collections;
import java.util.List;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.assertj.core.util.Lists;

import nl.knaw.huygens.Log;

public class CollectingErrorListener extends BaseErrorListener {
  List<SyntaxError> errors = Lists.newArrayList();

  @Override
  public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
    logRuleStack(recognizer);
    errors.add(new SyntaxError()//
        .withOffendingSymbol(offendingSymbol)//
        .withLine(line)//
        .withCharPositionInLine(charPositionInLine)//
        .withMsg(msg));
  }

  public List<SyntaxError> getErrors() {
    return errors;
  }

  private void logRuleStack(Recognizer<?, ?> recognizer) {
    List<String> stack = ((Parser) recognizer).getRuleInvocationStack();
    Collections.reverse(stack);
    Log.error("rule stack: " + stack);
  }

}
