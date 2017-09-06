package nl.knaw.huygens.antioch.util;

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

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import nl.knaw.huygens.antioch.service.AntiochService;

public class CleanupCommand implements Runnable {
  private static final Logger LOG = LoggerFactory.getLogger(CleanupCommand.class);
  private final AntiochService service;
  // private ProcessStatusMap<TextImportStatus> taskStatusMap;

  @Inject
  public CleanupCommand(AntiochService service/* , ProcessStatusMap<TextImportStatus> taskStatusMap */) {
    this.service = service;
    // this.taskStatusMap = taskStatusMap;
  }

  @Override
  public void run() {
    Preconditions.checkNotNull(service);
    // Preconditions.checkNotNull(taskStatusMap);
    // LOG.info("removing expired textImport statuses");
    // taskStatusMap.removeExpiredTasks();
    LOG.info("removing expired tentatives");
    service.removeExpiredTentatives();
  }
}
