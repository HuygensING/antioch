package nl.knaw.huygens.alexandria.antlr;

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
import org.slf4j.Logger;

import com.google.common.collect.Lists;

public class QueryErrorListener extends BaseErrorListener {
  private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(QueryErrorListener.class);
  private Collection<String> parseErrors = Lists.newArrayList();

  @Override
  public void reportAmbiguity(Parser recognizer, DFA dfa, int startIndex, int stopIndex, boolean exact, BitSet ambigAlts, ATNConfigSet configs) {
    LOG.warn("reportAmbiguity:");
    LOG.warn("recognizer={}", recognizer);
    LOG.warn("dfa={}", dfa);
    LOG.warn("startIndex={}", startIndex);
    LOG.warn("stopIndex={}", stopIndex);
    LOG.warn("exact={}", exact);
    LOG.warn("ambigAlts={}", ambigAlts);
    LOG.warn("configs={}", configs);
    parseErrors.add("ambiguity at (" + startIndex + ".." + stopIndex + ")");
  }

  @Override
  public void reportContextSensitivity(Parser recognizer, DFA dfa, int startIndex, int stopIndex, int prediction, ATNConfigSet configs) {
    LOG.warn("reportContextSensitivity:");
    LOG.warn("recognizer={}", recognizer);
    LOG.warn("dfa={}", dfa);
    LOG.warn("startIndex={}", startIndex);
    LOG.warn("stopIndex={}", stopIndex);
    LOG.warn("prediction={}", prediction);
    LOG.warn("configs={}", configs);
    parseErrors.add("contextSensitivity at (" + startIndex + ".." + stopIndex + ")");
  }

  @Override
  public void reportAttemptingFullContext(Parser recognizer, DFA dfa, int startIndex, int stopIndex, BitSet conflictingAlts, ATNConfigSet configs) {
    LOG.warn("reportAttemptingFullContext:");
    LOG.warn("recognizer={}", recognizer);
    LOG.warn("dfa={}", dfa);
    LOG.warn("startIndex={}", startIndex);
    LOG.warn("stopIndex={}", stopIndex);
    LOG.warn("conflictingAlts={}", conflictingAlts);
    LOG.warn("configs={}", configs);
    parseErrors.add("attemptingFullContext at (" + startIndex + ".." + stopIndex + ")");
  }

  @Override
  public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
    LOG.warn("syntaxError:");
    LOG.warn("recognizer={}", recognizer);
    LOG.warn("offendingSymbol={}", offendingSymbol);
    LOG.warn("line={}", line);
    LOG.warn("charPositionInLine={}", charPositionInLine);
    LOG.warn("msg={}", msg);
    // LOG.warn("e={}", e);
    parseErrors.add("syntax error at (" + line + ":" + charPositionInLine + ") " + msg);
  }

  public Collection<String> getParseErrors() {
    return parseErrors;
  }

  public boolean heardErrors() {
    return !parseErrors.isEmpty();
  }

}
