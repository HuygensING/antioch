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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import nl.knaw.huygens.alexandria.api.model.ProcessStatusMap;
import nl.knaw.huygens.alexandria.api.model.text.TextImportStatus;
import nl.knaw.huygens.alexandria.service.AlexandriaService;

public class CleanupCommand implements Runnable {
  static final Logger LOG = LoggerFactory.getLogger(CleanupCommand.class);
  private AlexandriaService service;
  private ProcessStatusMap<TextImportStatus> taskStatusMap;

  @Inject
  public CleanupCommand(AlexandriaService service, ProcessStatusMap<TextImportStatus> taskStatusMap) {
    this.service = service;
    this.taskStatusMap = taskStatusMap;
  }

  @Override
  public void run() {
    Preconditions.checkNotNull(service);
    Preconditions.checkNotNull(taskStatusMap);
    LOG.info("removing expired textImport statuses");
    taskStatusMap.removeExpiredTasks();
    LOG.info("removing expired tentatives");
    service.removeExpiredTentatives();
  }
}
