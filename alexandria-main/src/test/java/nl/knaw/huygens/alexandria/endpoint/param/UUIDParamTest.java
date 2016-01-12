package nl.knaw.huygens.alexandria.endpoint.param;

/*
 * #%L
 * alexandria-main
 * =======
 * Copyright (C) 2015 - 2016 Huygens ING (KNAW)
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

import static org.junit.Assert.assertEquals;
import java.util.UUID;
import nl.knaw.huygens.alexandria.endpoint.UUIDParam;
import nl.knaw.huygens.alexandria.exception.BadRequestException;
import org.junit.Test;

public class UUIDParamTest {
  private static final String A_VALID_UUID = "ea71773e-cbaf-11e4-a5af-473cb94dfabd";

  @Test(expected = BadRequestException.class)
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
