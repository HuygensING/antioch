package nl.knaw.huygens.alexandria.antlr;

public class SyntaxError {
  private int line;
  private int charPositionInLine;
  private Object offendingSymbol;
  private String msg;

  public SyntaxError withLine(int line) {
    this.line = line;
    return this;
  }

  public SyntaxError withCharPositionInLine(int charPositionInLine) {
    this.charPositionInLine = charPositionInLine;
    return this;
  }

  public SyntaxError withOffendingSymbol(Object offendingSymbol) {
    this.offendingSymbol = offendingSymbol;
    return this;
  }

  public SyntaxError withMsg(String msg) {
    this.msg = msg;
    return this;
  }

  public int getLine() {
    return line;
  }

  public int getCharPositionInLine() {
    return charPositionInLine;
  }

  public Object getOffendingSymbol() {
    return offendingSymbol;
  }

  public String getMsg() {
    return msg;
  }

  @Override
  public String toString() {
    return "syntax error: " + msg + " at " + line + ":" + charPositionInLine + " " + offendingSymbol;
  }
}