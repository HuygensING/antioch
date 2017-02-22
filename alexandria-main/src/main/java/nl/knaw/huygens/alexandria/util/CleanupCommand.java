package nl.knaw.huygens.alexandria.util;

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

import com.google.common.base.Preconditions;
import nl.knaw.huygens.alexandria.service.AlexandriaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

public class CleanupCommand implements Runnable {
  static final Logger LOG = LoggerFactory.getLogger(CleanupCommand.class);
  private AlexandriaService service;

  @Inject
  public CleanupCommand(AlexandriaService service) {
    this.service = service;
  }

  @Override
  public void run() {
    Preconditions.checkNotNull(service);
    LOG.info("removing expired tentatives");
    service.removeExpiredTentatives();
  }
}
