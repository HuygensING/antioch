package nl.knaw.huygens.alexandria.api.model.text;

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

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.Test;

import nl.knaw.huygens.alexandria.api.model.CommandResponse;
import nl.knaw.huygens.alexandria.endpoint.command.ResourceViewId;
import nl.knaw.huygens.alexandria.api.model.text.ResourcesCommand;

public class ResourcesCommandTest {

  class TestCommand extends ResourcesCommand {

    @Override
    public String getName() {
      return null;
    }

    @Override
    public CommandResponse runWith(Map<String, Object> parameterMap) {
      return null;
    }
  }

  @Test
  public void testSplit() throws Exception {
    TestCommand command = new TestCommand();
    String uuidString = "3e8c6332-230c-4fc5-865f-0d51534f4375";
    List<ResourceViewId> resourceViewIds = command.split(uuidString + ":view-1,view-2");
    ResourceViewId rvi1 = new ResourceViewId(UUID.fromString(uuidString), "view-1");
    ResourceViewId rvi2 = new ResourceViewId(UUID.fromString(uuidString), "view-2");
    assertThat(resourceViewIds).containsExactly(rvi1, rvi2);
  }

}
