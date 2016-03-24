package nl.knaw.huygens.alexandria.util;

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

import javax.inject.Inject;

import com.google.common.base.Preconditions;

import nl.knaw.huygens.Log;
import nl.knaw.huygens.alexandria.service.AlexandriaService;
import nl.knaw.huygens.alexandria.text.TaskStatusMap;

public class CleanupCommand implements Runnable {
  private AlexandriaService service;
  private TaskStatusMap taskStatusMap;

  @Inject
  public CleanupCommand(AlexandriaService service, TaskStatusMap taskStatusMap) {
    this.service = service;
    this.taskStatusMap = taskStatusMap;
  }

  @Override
  public void run() {
    Preconditions.checkNotNull(service);
    Preconditions.checkNotNull(taskStatusMap);
    Log.info("removing expired textImport statuses");
    taskStatusMap.removeExpiredTasks();
    Log.info("removing expired tentatives");
    service.removeExpiredTentatives();
  }
}
