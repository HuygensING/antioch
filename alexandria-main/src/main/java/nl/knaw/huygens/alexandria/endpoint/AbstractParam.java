package nl.knaw.huygens.alexandria.endpoint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

import nl.knaw.huygens.alexandria.exception.BadRequestException;

public abstract class AbstractParam<V> {
  private final static Logger LOG = LoggerFactory.getLogger(AbstractParam.class);
  private final V value;
  private final String originalParam;

  public AbstractParam(String param) throws BadRequestException {
    this.originalParam = param;
    try {
      this.value = parse(param);
    } catch (Throwable e) {
      final String errorMessage = getErrorMessage(param, e);
      LOG.warn("Failed to parse: [{}]: {}", param, errorMessage);
      throw new BadRequestException(errorMessage);
    }
  }

  public V getValue() {
    return value;
  }

  public String getOriginalParam() {
    return originalParam;
  }

  @Override
  public String toString() {
    return value.toString();
  }

  protected abstract V parse(String param) throws Throwable;

  protected String getErrorMessage(String param, Throwable e) {
    return String.format("Invalid parameter: %s (%s)", param, e.getMessage());
  }
}
