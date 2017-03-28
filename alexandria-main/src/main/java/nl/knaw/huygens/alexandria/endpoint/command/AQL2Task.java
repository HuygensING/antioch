package nl.knaw.huygens.alexandria.endpoint.command;

/*
 * #%L
 * alexandria-main
 * =======
 * Copyright (C) 2015 - 2017 Huygens ING (KNAW)
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

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

import com.google.common.base.Joiner;

import nl.knaw.huygens.alexandria.antlr.AQL2Lexer;
import nl.knaw.huygens.alexandria.antlr.AQL2Parser;
import nl.knaw.huygens.alexandria.antlr.QueryErrorListener;
import nl.knaw.huygens.alexandria.api.model.CommandStatus;

public class AQL2Task implements Runnable {
  private CommandStatus status;
  private UUID uuid;
  private String aql2Command;

  public AQL2Task(String aql2Command) {
    this.aql2Command = aql2Command;
    this.uuid = UUID.randomUUID();
    this.status = new CommandStatus();
  }

  public CommandStatus getStatus() {
    return status;
  }

  @Override
  public void run() {
    status.setStarted();
    try {
      Object result = process();
      status.setResult(result);
      status.setSuccess(true);

    } catch (Exception e) {
      e.printStackTrace();
      status.setSuccess(false);
      status.setErrorMessage(e.getMessage());
    }
    status.setDone();
  }

  private Object process() {
    QueryVisitor visitor = new QueryVisitor();

    List<String> parseErrors = parse(visitor);

    if (!parseErrors.isEmpty()) {
      throw new RuntimeException("parse error(s): " + Joiner.on("\n").join(parseErrors));
    }

    String function = visitor.getFunction();
    List<Object> parameters = visitor.getParameters();

    Object result;
    switch (function) {
    case "hello":
      result = parameters.stream().map(this::hello).collect(joining("\n"));
      break;

    case "bye":
      result = parameters.stream().map(this::bye).collect(joining("\n"));
      break;

    default:
      throw new RuntimeException("unknown command: " + aql2Command);
    }
    return result;
  }

  private List<String> parse(QueryVisitor visitor) {
    List<String> parseErrors = new ArrayList<>();
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
    // Log.info("tree={}", tree.toStringTree(parser));
    if (errorListener.heardErrors()) {
      parseErrors.addAll(errorListener.getParseErrors().stream()//
          // .map(AlexandriaQueryParser::clarifyParseError)//
          .collect(toList()));
      // result = "error";
    }

    visitor.visit(tree);
    parseErrors.addAll(errorListener.getParseErrors());
    return parseErrors;
  }

  public UUID getUUID() {
    return uuid;
  }

  public String hello(Object parameter) {
    return "Hello and welcome, " + parameter + "!";
  }

  public String bye(Object parameter) {
    return "Goodbye " + parameter + "!";
  }

}
