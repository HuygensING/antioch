package nl.knaw.huygens.alexandria.endpoint.command;

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

import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Optional;
import java.util.UUID;

public class ResourceViewId {
  private UUID resourceId;
  private String textViewName;

  public ResourceViewId(UUID resourceId, String textViewName) {
    this.resourceId = resourceId;
    this.textViewName = textViewName;
  }

  public static ResourceViewId fromString(String idString) {
    String[] parts = idString.split(":");
    UUID uuid = UUID.fromString(parts[0]);
    String name = (parts.length > 1) ? parts[1] : null;
    return new ResourceViewId(uuid, name);
  }

  public UUID getResourceId() {
    return resourceId;
  }

  public Optional<String> getTextViewName() {
    return Optional.ofNullable(textViewName);
  }

  @Override
  public String toString() {
    return resourceId.toString() + (textViewName == null ? "" : ":" + textViewName);
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof ResourceViewId //
        && ((ResourceViewId) obj).getTextViewName().equals(getTextViewName()) //
        && ((ResourceViewId) obj).getResourceId().toString().equals(resourceId.toString());
  }

  @Override
  public int hashCode() {
    return HashCodeBuilder.reflectionHashCode(this, false);
  }

}
