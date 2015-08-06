package nl.knaw.huygens.alexandria.query;

import java.util.List;

public class AlexandriaQueryParseException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  public AlexandriaQueryParseException(List<String> parseErrors) {
    super(buildMessage(parseErrors));
  }

  private static String buildMessage(List<String> parseErrors) {
    StringBuilder builder = new StringBuilder("parse errors:\n");
    int i = 1;
    for (String error : parseErrors) {
      builder.append(i++).append(": ").append(error).append("\n");
    }
    return builder.toString();
  }

}
