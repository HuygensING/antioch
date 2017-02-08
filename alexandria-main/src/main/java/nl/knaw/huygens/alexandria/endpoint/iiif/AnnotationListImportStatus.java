package nl.knaw.huygens.alexandria.endpoint.iiif;

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

import com.google.common.collect.Lists;
import nl.knaw.huygens.alexandria.api.model.ProcessStatus;

import java.util.List;
import java.util.Map;

public class AnnotationListImportStatus extends ProcessStatus {
  private Map<String, Object> processedList;
  private List<String> errors = Lists.newArrayList();

  public void setProcessedList(Map<String, Object> processedList) {
    this.processedList = processedList;
  }

  public Map<String, Object> getProcessedList() {
    return processedList;
  }

  public List<String> getErrors() {
    return errors;
  }

  public boolean hasErrors() {
    return errors.size() > 0;
  }

}
