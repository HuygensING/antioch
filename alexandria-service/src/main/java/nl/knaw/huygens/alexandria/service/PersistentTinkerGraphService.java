package nl.knaw.huygens.alexandria.service;

/*
 * #%L
 * alexandria-service
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
