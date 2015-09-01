package nl.knaw.huygens.alexandria.query;

import java.util.BitSet;
import java.util.Collection;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.atn.ATNConfigSet;
import org.antlr.v4.runtime.dfa.DFA;

import com.google.common.collect.Lists;

import nl.knaw.huygens.Log;

public class QueryErrorListener extends BaseErrorListener {
  private Collection<String> parseErrors = Lists.newArrayList();

  @Override
  public void reportAmbiguity(Parser recognizer, DFA dfa, int startIndex, int stopIndex, boolean exact, BitSet ambigAlts, ATNConfigSet configs) {
    Log.warn("reportAmbiguity:");
    Log.warn("recognizer={}", recognizer);
    Log.warn("dfa={}", dfa);
    Log.warn("startIndex={}", startIndex);
    Log.warn("stopIndex={}", stopIndex);
    Log.warn("exact={}", exact);
    Log.warn("ambigAlts={}", ambigAlts);
    Log.warn("configs={}", configs);
    parseErrors.add("where: ambiguity at (" + startIndex + ".." + stopIndex + ")");
  }

  @Override
  public void reportContextSensitivity(Parser recognizer, DFA dfa, int startIndex, int stopIndex, int prediction, ATNConfigSet configs) {
    Log.warn("reportContextSensitivity:");
    Log.warn("recognizer={}", recognizer);
    Log.warn("dfa={}", dfa);
    Log.warn("startIndex={}", startIndex);
    Log.warn("stopIndex={}", stopIndex);
    Log.warn("prediction={}", prediction);
    Log.warn("configs={}", configs);
    parseErrors.add("where: contextSensitivity at (" + startIndex + ".." + stopIndex + ")");
  }

  @Override
  public void reportAttemptingFullContext(Parser recognizer, DFA dfa, int startIndex, int stopIndex, BitSet conflictingAlts, ATNConfigSet configs) {
    Log.warn("reportAttemptingFullContext:");
    Log.warn("recognizer={}", recognizer);
    Log.warn("dfa={}", dfa);
    Log.warn("startIndex={}", startIndex);
    Log.warn("stopIndex={}", stopIndex);
    Log.warn("conflictingAlts={}", conflictingAlts);
    Log.warn("configs={}", configs);
    parseErrors.add("where: attemptingFullContext at (" + startIndex + ".." + stopIndex + ")");
  }

  @Override
  public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
    Log.warn("syntaxError:");
    Log.warn("recognizer={}", recognizer);
    Log.warn("offendingSymbol={}", offendingSymbol);
    Log.warn("line={}", line);
    Log.warn("charPositionInLine={}", charPositionInLine);
    Log.warn("msg={}", msg);
    // Log.warn("e={}", e);
    parseErrors.add("where: (" + line + ":" + charPositionInLine + ") " + msg);
  }

  public Collection<String> getParseErrors() {
    return parseErrors;
  }

  public boolean heardErrors() {
    return !parseErrors.isEmpty();
  }

}
