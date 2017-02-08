package nl.knaw.huygens.alexandria.endpoint.webannotation;

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

import java.util.UUID;

public class WebAnnotation {

  private String json = "";
  private String eTag = "";
  private UUID uuid;

  public WebAnnotation(UUID id) {
    this.uuid = id;
  }

  public WebAnnotation setJson(String json) {
    this.json = json;
    return this;
  }

  public String json() {
    return json;
  }

  public WebAnnotation setETag(String eTag) {
    this.eTag = eTag;
    return this;
  }

  public String eTag() {
    return eTag;
  }

  public UUID getId() {
    return uuid;
  }



}
