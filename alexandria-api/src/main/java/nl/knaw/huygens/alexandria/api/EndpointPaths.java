package nl.knaw.huygens.alexandria.api;

/*
 * #%L
 * alexandria-api
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

public final class EndpointPaths {
  public static final String ABOUT = "about";

  public static final String RESOURCES = "resources";
  public static final String SUBRESOURCES = "subresources";
  public static final String TEXT = "text";
  public static final String TEXTVIEWS = "views";

  public static final String ANNOTATIONS = "annotations";
  public static final String REV = "rev";
  public static final String ANNOTATIONBODIES = "annotationbodies";

  public static final String SEARCHES = "searches";

  public static final String COMMANDS = "commands";

  public static final String RESULTPAGES = "pages";

  public static final String ANNOTATORS = "annotators";

  private EndpointPaths() {
    throw new AssertionError("Paths shall not be instantiated");
  }

}
