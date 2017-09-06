package nl.knaw.huygens.antioch.api;

/*
 * #%L
 * antioch-api
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

public final class EndpointPaths {
  public static final String ABOUT = "about";

  public static final String RESOURCES = "resources";
  public static final String SUBRESOURCES = "subresources";
  public static final String TEXT = "text";
  public static final String TEXTVIEWS = "views";

  public static final String ANNOTATIONS = "annotations";
  public static final String ANNOTATIONBATCH = "annotationbatch";
  public static final String REV = "rev";
  public static final String ANNOTATIONBODIES = "annotationbodies";

  public static final String SEARCHES = "searches";

  public static final String COMMANDS = "commands";

  public static final String RESULTPAGES = "pages";

  public static final String ANNOTATORS = "annotators";

  public static final String WEB_ANNOTATIONS = "webannotations";
  public static final String IIIF = "iiif";

  private EndpointPaths() {
    throw new AssertionError("Paths shall not be instantiated");
  }

}
