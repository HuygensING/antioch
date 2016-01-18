package nl.knaw.huygens.alexandria.endpoint.resource.text;

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

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeName;

import nl.knaw.huygens.alexandria.endpoint.JsonWrapperObject;
import nl.knaw.huygens.alexandria.endpoint.Prototype;

@JsonTypeName("textQuery")
public class TextQuery extends JsonWrapperObject implements Prototype {
  @JsonIgnore
  private UUID resourceUUID;

  private String type = "";
  private String query = "";

  public void setResourceUUID(UUID resourceUUID) {
    this.resourceUUID = resourceUUID;
  }

  public UUID getResourceUUID() {
    return resourceUUID;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getType() {
    return type;
  }

  public void setQuery(String query) {
    this.query = query;
  }

  public String getQuery() {
    return query;
  }

}
