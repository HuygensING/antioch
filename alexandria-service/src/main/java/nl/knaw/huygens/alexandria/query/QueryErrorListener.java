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
