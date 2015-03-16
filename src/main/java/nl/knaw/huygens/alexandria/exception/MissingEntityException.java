package nl.knaw.huygens.alexandria.exception;

public class MissingEntityException extends BadRequestException {
  public MissingEntityException() {
    super("Missing entity (empty request body)");
  }
}
