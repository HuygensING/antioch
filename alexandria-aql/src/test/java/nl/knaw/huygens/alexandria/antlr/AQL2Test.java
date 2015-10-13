package nl.knaw.huygens.alexandria.antlr;

/*
 * #%L
 * alexandria-aql
 * =======
 * Copyright (C) 2015 Huygens ING (KNAW)
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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.Test;

import nl.knaw.huygens.Log;

public class AQL2Test {

  @Test
  public void testCorrectAQLStatement() {
    String statement = "find annotation" //
        + " where"//
        + " type:eq(\"Tag\")"//
        + " and who:eq(\"nedlab\")" //
        + " and state:eq(\"CONFIRMED\")"//
        + " and resource.id:inSet(1,2)"//
        + " and value:match(\"what.*\")"//
        + " and when:inRange(\"2015-01-01T00:00:00Z\",\"2016-01-01T00:00:00Z\")"//
        + " sort on -id" //
        + " return id,who,when,type,value";
    CharStream stream = new ANTLRInputStream(statement);
    AQL2Lexer lex = new AQL2Lexer(stream);
    List<? extends Token> allTokens = lex.getAllTokens();
    for (Token token : allTokens) {
      Log.info("token: [{}] <<{}>>", lex.getRuleNames()[token.getType() - 1], token.getText());
    }
    lex.reset();

    CommonTokenStream tokens = new CommonTokenStream(lex);
    AQL2Parser parser = new AQL2Parser(tokens);
    parser.setBuildParseTree(true);
    ParseTree tree = parser.query();
    Log.info("tree={}", tree.toStringTree(parser));
    assertThat(tree.getChildCount()).isEqualTo(4); // find, where, sort, return

    int numberOfSyntaxErrors = parser.getNumberOfSyntaxErrors();
    assertThat(numberOfSyntaxErrors).isEqualTo(0); // no syntax errors
    Log.info("numberOfSyntaxErrors={}", numberOfSyntaxErrors);

    for (int i = 0; i < tree.getChildCount(); i++) {
      Log.info("root.child={}", tree.getChild(i).getText());
    }
    assertThat(allTokens).hasSize(55);
  }

  @Test
  public void testSpacesInStringsAreParsedCorrectly() {
    String statement = "find annotation where type:eq(\"Tag\")"//
        + " and who:eq(\"Jan Klaassen\")" //
        + " and value:eq(\"code: red\")" //
        ;
    CharStream stream = new ANTLRInputStream(statement);
    AQL2Lexer lex = new AQL2Lexer(stream);
    List<? extends Token> allTokens = lex.getAllTokens();
    for (Token token : allTokens) {
      Log.info("token: [{}] <<{}>>", lex.getRuleNames()[token.getType() - 1], token.getText());
    }
    lex.reset();

    CommonTokenStream tokens = new CommonTokenStream(lex);
    AQL2Parser parser = new AQL2Parser(tokens);
    parser.setBuildParseTree(true);
    ParseTree tree = parser.query();
    Log.info("tree={}", tree.toStringTree(parser));

    int numberOfSyntaxErrors = parser.getNumberOfSyntaxErrors();
    assertThat(numberOfSyntaxErrors).isEqualTo(0); // no syntax errors
    Log.info("numberOfSyntaxErrors={}", numberOfSyntaxErrors);

    assertThat(tree.getChildCount()).isEqualTo(2); // find, where

    for (int i = 0; i < tree.getChildCount(); i++) {
      Log.info("root.child={}", tree.getChild(i).getText());
    }
    assertThat(allTokens).hasSize(20);
  }

  @Test
  public void testAQLStatementWithBadFindArgument() {
    String statement = "find whatever" //
        + " where"//
        + " type:eq(\"Tag\")"//
        + " return id,who,when,type,value";
    CharStream stream = new ANTLRInputStream(statement);
    AQL2Lexer lex = new AQL2Lexer(stream);
    lex.removeErrorListeners();
    List<? extends Token> allTokens = lex.getAllTokens();
    for (Token token : allTokens) {
      Log.info("token: [{}] <<{}>>", lex.getRuleNames()[token.getType() - 1], token.getText());
    }
    lex.reset();

    CommonTokenStream tokens = new CommonTokenStream(lex);
    AQL2Parser parser = new AQL2Parser(tokens);
    parser.removeErrorListeners();
    CollectingErrorListener errorListener = new CollectingErrorListener();
    parser.addErrorListener(errorListener);
    parser.setBuildParseTree(true);
    ParseTree tree = parser.query();
    Log.info("tree={}", tree.toStringTree(parser));
    assertThat(tree.getChildCount()).isEqualTo(3); // find, where, return

    int numberOfSyntaxErrors = parser.getNumberOfSyntaxErrors();
    assertThat(numberOfSyntaxErrors).isEqualTo(1); // no syntax errors
    Log.info("numberOfSyntaxErrors={}", numberOfSyntaxErrors);

    List<SyntaxError> errors = errorListener.getErrors();
    assertThat(errors).hasSize(1);
    Log.info("errors:{}", errors);

    for (int i = 0; i < tree.getChildCount(); i++) {
      Log.info("root.child={}", tree.getChild(i).getText());
    }
    // assertThat(allTokens).hasSize(55);
  }

}
