package nl.knaw.huygens.alexandria.api.model.text;

/*
 * #%L
 * alexandria-api
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

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.google.common.collect.Lists;

import nl.knaw.huygens.alexandria.api.JsonTypeNames;
import nl.knaw.huygens.alexandria.api.model.ProcessStatus;

@JsonTypeName(JsonTypeNames.TEXTANNOTATIONIMPORTSTATUS)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TextAnnotationImportStatus extends ProcessStatus {
  private List<String> errors = Lists.newArrayList();
  private Map<UUID, TextRangeAnnotationInfo> textRangeAnnotationInfoMap = new ConcurrentHashMap<>();
  private String breakingErrorMessage;

  public List<String> getErrors() {
    return errors;
  }

  public void setBreakingErrorMessage(String breakingErrorMessage) {
    this.breakingErrorMessage = breakingErrorMessage;
  }

  public boolean hasErrors() {
    return errors.size() > 0;
  }

  public String getBreakingErrorMessage() {
    return breakingErrorMessage;
  }

  public Map<UUID, TextRangeAnnotationInfo> getTextRangeAnnotationInfoMap() {
    return textRangeAnnotationInfoMap;
  }

}
