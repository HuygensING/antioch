package nl.knaw.huygens.alexandria.antlr;

/*
 * #%L
 * alexandria-main
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
  private final Collection<String> parseErrors = Lists.newArrayList();

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
