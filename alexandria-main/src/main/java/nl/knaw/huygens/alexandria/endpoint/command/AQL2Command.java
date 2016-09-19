package nl.knaw.huygens.alexandria.endpoint.command;

import static java.util.stream.Collectors.joining;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

import nl.knaw.huygens.Log;
import nl.knaw.huygens.alexandria.antlr.AQL2Lexer;
import nl.knaw.huygens.alexandria.antlr.AQL2Parser;
import nl.knaw.huygens.alexandria.antlr.QueryErrorListener;
import nl.knaw.huygens.alexandria.api.model.CommandResponse;
import nl.knaw.huygens.alexandria.api.model.Commands;
import nl.knaw.huygens.alexandria.service.AlexandriaService;

public class AQL2Command extends ResourcesCommand {

  public static final String COMMAND_PARAMETER = "command";
  private CommandResponse commandResponse = new CommandResponse();
  private AlexandriaService service;

  @Inject
  public AQL2Command(AlexandriaService service) {
    this.service = service;
  }

  @Override
  public String getName() {
    return Commands.AQL2;
  }

  @Override
  public CommandResponse runWith(Map<String, Object> parameterMap) {
    String aql2Command = (String) parameterMap.get(COMMAND_PARAMETER);
    String result = process(aql2Command);
    commandResponse.setResult(result);
    commandResponse.setParametersAreValid(true);
    return commandResponse;
  }

  private String process(String aql2Command) {
    QueryErrorListener errorListener = new QueryErrorListener();
    CharStream stream = new ANTLRInputStream(aql2Command);
    AQL2Lexer lex = new AQL2Lexer(stream);
    lex.removeErrorListeners();
    CommonTokenStream tokenStream = new CommonTokenStream(lex);
    AQL2Parser parser = new AQL2Parser(tokenStream);
    parser.removeErrorListeners();
    parser.addErrorListener(errorListener);
    parser.setBuildParseTree(true);
    ParseTree tree = parser.root();
    Log.info("tree={}", tree.toStringTree(parser));
    if (errorListener.heardErrors()) {
      // parseErrors.addAll(errorListener.getParseErrors().stream()//
      // .map(AlexandriaQueryParser::clarifyParseError)//
      // .collect(toList()));
      return "error";
    }

    QueryVisitor visitor = new QueryVisitor();
    visitor.visit(tree);
    // parseErrors.addAll(errorListener.getParseErrors());

    String function = visitor.getFunction();
    List<Object> parameters = visitor.getParameters();

    switch (function) {
    case "hello":
      return parameters.stream().map(this::hello).collect(joining("\n"));

    case "bye":
      return parameters.stream().map(this::bye).collect(joining("\n"));

    default:
      return "Unknown Command: " + aql2Command;
    }

  }

  public String hello(Object parameter) {
    return "Hello and welcome, " + parameter + "!";
  }

  public String bye(Object parameter) {
    return "Goodbye " + parameter + "!";
  }

}
