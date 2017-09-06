package nl.knaw.huygens.antioch.endpoint.param;

/*
 * #%L
 * antioch-main
 * =======
 * Copyright (C) 2015 - 2017 Huygens ING (KNAW)
 * =======
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import static org.junit.Assert.assertEquals;
import java.util.UUID;
import nl.knaw.huygens.antioch.endpoint.UUIDParam;
import nl.knaw.huygens.antioch.exception.BadRequestException;
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
    assertEquals(A_VALID_UUID, new UUIDParam(A_VALID_UUID).getOriginalParam());
  }

  @Test
  public void testParamIsConvertedToValue() {
    final UUID expected = UUID.fromString(A_VALID_UUID);
    assertEquals(expected, new UUIDParam(A_VALID_UUID).getValue());
  }
}
