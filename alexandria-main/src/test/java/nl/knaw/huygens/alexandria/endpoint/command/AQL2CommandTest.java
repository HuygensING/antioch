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
import static org.mockito.Mockito.mock;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.Test;

import com.google.common.collect.ImmutableMap;

import nl.knaw.huygens.alexandria.api.model.CommandResponse;
import nl.knaw.huygens.alexandria.api.model.CommandStatus;
import nl.knaw.huygens.alexandria.api.model.ProcessStatusMap;
import nl.knaw.huygens.alexandria.config.AlexandriaConfiguration;
import nl.knaw.huygens.alexandria.config.MockConfiguration;
import nl.knaw.huygens.alexandria.service.AlexandriaService;
import nl.knaw.huygens.alexandria.test.AlexandriaTest;

public class AQL2CommandTest extends AlexandriaTest {

  @Test
  public void testRunWith() throws Exception {
    AlexandriaService service = mock(AlexandriaService.class);
    AlexandriaConfiguration config = new MockConfiguration();
    ExecutorService executorService = Executors.newSingleThreadExecutor();
    ProcessStatusMap<CommandStatus> taskStatusMap = new ProcessStatusMap<>();
    AQL2Command ac = new AQL2Command(service, config, executorService, taskStatusMap);
    Map<String, Object> parameterMap = ImmutableMap.of(AQL2Command.COMMAND_PARAMETER, "hello(\"World\",\"You\")");
    CommandResponse response = ac.runWith(parameterMap);
    UUID statusId = response.getStatusId();
    assertThat(statusId).isNotNull();
    assertThat(response.isASync()).isTrue();
    Optional<CommandStatus> status = taskStatusMap.get(statusId);
    while (!status.get().isDone()) {
      Thread.sleep(100);
      status = taskStatusMap.get(statusId);
    }
    Object endResult = status.get().getResult();
    assertThat(endResult).isEqualTo("Hello and welcome, World!\nHello and welcome, You!");
  }

}
