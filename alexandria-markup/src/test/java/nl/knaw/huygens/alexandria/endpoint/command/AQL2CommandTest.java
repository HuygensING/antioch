package nl.knaw.huygens.alexandria.endpoint.command;

import nl.knaw.huygens.alexandria.test.AlexandriaTest;

public class AQL2CommandTest extends AlexandriaTest {

  // @Test
  // public void testRunWith() throws Exception {
  // AlexandriaService service = mock(AlexandriaService.class);
  // AlexandriaConfiguration config = new MockConfiguration();
  // ExecutorService executorService = Executors.newSingleThreadExecutor();
  // ProcessStatusMap<CommandStatus> taskStatusMap = new ProcessStatusMap<>();
  // AQL2Command ac = new AQL2Command(service, config, executorService, taskStatusMap);
  // Map<String, Object> parameterMap = ImmutableMap.of(AQL2Command.COMMAND_PARAMETER, "hello(\"World\",\"You\")");
  // CommandResponse response = ac.runWith(parameterMap);
  // UUID statusId = response.getStatusId();
  // assertThat(statusId).isNotNull();
  // assertThat(response.isASync()).isTrue();
  // Optional<CommandStatus> status = taskStatusMap.get(statusId);
  // while (!status.get().isDone()) {
  // Thread.sleep(100);
  // status = taskStatusMap.get(statusId);
  // }
  // Object endResult = status.get().getResult();
  // assertThat(endResult).isEqualTo("Hello and welcome, World!\nHello and welcome, You!");
  // }

}
