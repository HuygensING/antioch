package nl.knaw.huygens.alexandria.exception;

public class ExceptionErrorEntity extends ErrorEntity {

  private Exception exception;

  private ExceptionErrorEntity(Exception e) {
    super(e.getMessage());
    this.exception = e;
  }

  public static ExceptionErrorEntity of(Exception e) {
    return new ExceptionErrorEntity(e);
  }

  public String getException() {
    return exception.getClass().getName();
  }

}
