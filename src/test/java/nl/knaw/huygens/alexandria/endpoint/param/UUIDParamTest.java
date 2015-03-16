package nl.knaw.huygens.alexandria.endpoint.param;

import javax.ws.rs.WebApplicationException;

import org.junit.Test;

public class UUIDParamTest {
  @Test
  public void testConstructorAcceptsValidUUID() {
    new UUIDParam("ea71773e-cbaf-11e4-a5af-473cb94dfabd");
  }

  @Test(expected = WebApplicationException.class)
  public void testConstructorRejectsInvalidUUID() {
    new UUIDParam("invalid-uuid");
  }

}