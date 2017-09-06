package nl.knaw.huygens.antioch.endpoint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

import nl.knaw.huygens.antioch.exception.BadRequestException;

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
