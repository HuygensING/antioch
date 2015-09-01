package nl.knaw.huygens.alexandria.antlr;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.StrictAssertions.assertThat;

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
    String statement = "type:eq(\"Tag\"),"//
        + " who:eq(\"nedlab\")," //
        + " state:eq(\"CONFIRMED\"),"//
        + " resource.id:inSet(1,2),"//
        + " value:match(\"whatever\")";
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
    assertThat(tree.getChildCount()).isEqualTo(9); // 5 subqueries + 4 commas

    int numberOfSyntaxErrors = parser.getNumberOfSyntaxErrors();
    assertThat(numberOfSyntaxErrors).isEqualTo(0); // no syntax errors
    Log.info("numberOfSyntaxErrors={}", numberOfSyntaxErrors);

    for (int i = 0; i < tree.getChildCount(); i++) {
      Log.info("root.child={}", tree.getChild(i).getText());
    }
    assertThat(allTokens).hasSize(36);
  }

}
