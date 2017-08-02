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

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class ResourceViewIdTest {

  @Test
  public void testFromStringWithViewName() throws Exception {
    String uuidString = "3e8c6332-230c-4fc5-865f-0d51534f4375";
    String viewName = "viewname";
    String idString = uuidString + ":" + viewName;
    ResourceViewId rvi = ResourceViewId.fromString(idString);
    assertThat(rvi.getResourceId().toString()).isEqualTo(uuidString);
    assertThat(rvi.getTextViewName()).isPresent();
    assertThat(rvi.getTextViewName().get()).isEqualTo(viewName);
    assertThat(rvi.toString()).isEqualTo(idString);
  }

  @Test
  public void testFromStringWithoutViewName() throws Exception {
    String uuidString = "3e8c6332-230c-4fc5-865f-0d51534f4376";
    ResourceViewId rvi = ResourceViewId.fromString(uuidString);
    assertThat(rvi.getResourceId().toString()).isEqualTo(uuidString);
    assertThat(rvi.getTextViewName()).isNotPresent();
    assertThat(rvi.toString()).isEqualTo(uuidString);
  }

}
