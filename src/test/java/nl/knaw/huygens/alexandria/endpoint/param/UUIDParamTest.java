package nl.knaw.huygens.alexandria.endpoint.param;

import static org.junit.Assert.assertEquals;

import javax.ws.rs.WebApplicationException;
import java.util.UUID;

import org.junit.Test;

public class UUIDParamTest {
  private static final String A_VALID_UUID = "ea71773e-cbaf-11e4-a5af-473cb94dfabd";

  @Test(expected = WebApplicationException.class)
  public void testConstructorRejectsInvalidUUID() {
    new UUIDParam("invalid-uuid");
  }

  @Test
  public void testConstructorAcceptsValidUUID() {
    new UUIDParam(A_VALID_UUID);
  }

  @Test
  public void testParamYieldsOriginalValue() {
    final String expected = A_VALID_UUID;
    assertEquals(expected, new UUIDParam(A_VALID_UUID).getOriginalParam());
  }

  @Test
  public void testParamIsConvertedToValue() {
    final UUID expected = UUID.fromString(A_VALID_UUID);
    assertEquals(expected, new UUIDParam(A_VALID_UUID).getValue());
  }
}