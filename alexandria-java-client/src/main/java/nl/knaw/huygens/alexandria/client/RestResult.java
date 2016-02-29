package nl.knaw.huygens.alexandria.client;

public class RestResult<T> {
  private boolean failure = false;
  private T cargo;

  public boolean hasFailed() {
    return failure;
  }

  public void setFail(boolean failure) {
    this.failure = failure;
  }

  public T get() {
    return cargo;
  }

  public void setCargo(T cargo) {
    this.cargo = cargo;
  }
}
