package nl.knaw.huygens.alexandria.model;

/*
 * #%L
 * alexandria-main
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

import java.time.Instant;

import nl.knaw.huygens.alexandria.jaxrs.ThreadContext;

public class TentativeAlexandriaProvenance {
  private final String who;
  private final Instant when;
  private final String why;

  public static TentativeAlexandriaProvenance createDefault() {
    return new TentativeAlexandriaProvenance(ThreadContext.getUserName(), Instant.now(), AlexandriaProvenance.DEFAULT_WHY);
  }

  public TentativeAlexandriaProvenance(String who, Instant when, String why) {
    this.who = who;
    this.when = when;
    this.why = why;
  }

  public String getWho() {
    return who;
  }

  public Instant getWhen() {
    return when;
  }

  public String getWhy() {
    return why;
  }

  public AlexandriaProvenance bind(Accountable what) {
    return new AlexandriaProvenance(what, who, when, why);
  }

}
