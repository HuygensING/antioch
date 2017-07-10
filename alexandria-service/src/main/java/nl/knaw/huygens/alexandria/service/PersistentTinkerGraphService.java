package nl.knaw.huygens.alexandria.service;

/*
 * #%L
 * alexandria-service
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

import java.nio.file.Files;
import java.nio.file.Paths;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.knaw.huygens.alexandria.config.AlexandriaConfiguration;
import nl.knaw.huygens.alexandria.endpoint.LocationBuilder;

@Singleton
public class PersistentTinkerGraphService extends TinkerGraphService {
  private static Logger LOG = LoggerFactory.getLogger(PersistentTinkerGraphService.class);
  private static final String DUMPFILE = "alexandria.gryo";

  @Inject
  public PersistentTinkerGraphService(AlexandriaConfiguration config, LocationBuilder locationBuilder) {
    super(locationBuilder);
    String dumpfile = config.getStorageDirectory() + "/" + DUMPFILE;
    STORAGE.setDumpFile(dumpfile);
    if (!STORAGE.supportsPersistence() && Files.exists(Paths.get(dumpfile))) {
      LOG.info("reading stored db from {}", dumpfile);
      STORAGE.loadFromDisk(dumpfile);
    }
  }
}
