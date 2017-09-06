package nl.knaw.huygens.antioch.antlr;

/*
 * #%L
 * antioch-aql
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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.Test;

import nl.knaw.huygens.Log;

public class AQLTest {

  @Test
  public void testCorrectAQLStatement() {
    String statement = "type:eq(\"Tag\")"//
        + " who:eq(\"nedlab\")" //
        + " state:eq(\"CONFIRMED\")"//
        + " resource.id:inSet(1,2)"//
        + " value:match(\"what.*\")"//
        + " when:inRange(\"2015-01-01T00:00:00Z\",\"2016-01-01T00:00:00Z\")"//
        ;
    CharStream stream = new ANTLRInputStream(statement);
    AQLLexer lex = new AQLLexer(stream);
    List<? extends Token> allTokens = lex.getAllTokens();
    for (Token token : allTokens) {
      Log.info("token: [{}] <<{}>>", lex.getRuleNames()[token.getType() - 1], token.getText());
    }
    lex.reset();

    CommonTokenStream tokens = new CommonTokenStream(lex);
    AQLParser parser = new AQLParser(tokens);
    parser.setBuildParseTree(true);
    ParseTree tree = parser.root();
    Log.info("tree={}", tree.toStringTree(parser));
    assertThat(tree.getChildCount()).isEqualTo(6); // 6 subqueries

    int numberOfSyntaxErrors = parser.getNumberOfSyntaxErrors();
    assertThat(numberOfSyntaxErrors).isEqualTo(0); // no syntax errors
    Log.info("numberOfSyntaxErrors={}", numberOfSyntaxErrors);

    for (int i = 0; i < tree.getChildCount(); i++) {
      Log.info("root.child={}", tree.getChild(i).getText());
    }
    assertThat(allTokens).hasSize(40);
  }

  @Test
  public void testSpacesInStringsAreParsedCorrectly() {
    String statement = "type:eq(\"Tag\")"//
        + " who:eq(\"Jan Klaassen\")" //
        + " value:eq(\"code: red\")" //
        ;
    CharStream stream = new ANTLRInputStream(statement);
    AQLLexer lex = new AQLLexer(stream);
    List<? extends Token> allTokens = lex.getAllTokens();
    for (Token token : allTokens) {
      Log.info("token: [{}] <<{}>>", lex.getRuleNames()[token.getType() - 1], token.getText());
    }
    lex.reset();

    CommonTokenStream tokens = new CommonTokenStream(lex);
    AQLParser parser = new AQLParser(tokens);
    parser.setBuildParseTree(true);
    ParseTree tree = parser.root();
    Log.info("tree={}", tree.toStringTree(parser));

    int numberOfSyntaxErrors = parser.getNumberOfSyntaxErrors();
    assertThat(numberOfSyntaxErrors).isEqualTo(0); // no syntax errors
    Log.info("numberOfSyntaxErrors={}", numberOfSyntaxErrors);

    assertThat(tree.getChildCount()).isEqualTo(3); // 2 subqueries

    for (int i = 0; i < tree.getChildCount(); i++) {
      Log.info("root.child={}", tree.getChild(i).getText());
    }
    assertThat(allTokens).hasSize(18);
  }

}
