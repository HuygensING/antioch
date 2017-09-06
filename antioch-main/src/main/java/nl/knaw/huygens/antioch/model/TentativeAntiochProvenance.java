package nl.knaw.huygens.antioch.model;

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

import java.time.Instant;

import nl.knaw.huygens.antioch.jaxrs.ThreadContext;

public class TentativeAntiochProvenance {
  private final String who;
  private final Instant when;
  private final String why;

  public static TentativeAntiochProvenance createDefault() {
    return new TentativeAntiochProvenance(ThreadContext.getUserName(), Instant.now(), AntiochProvenance.DEFAULT_WHY);
  }

  public TentativeAntiochProvenance(String who, Instant when, String why) {
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

  public AntiochProvenance bind(Accountable what) {
    return new AntiochProvenance(what, who, when, why);
  }

}
