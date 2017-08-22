package nl.knaw.huygens.alexandria.endpoint;

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
import java.util.Optional;

import nl.knaw.huygens.alexandria.jaxrs.ThreadContext;
import nl.knaw.huygens.alexandria.model.AlexandriaProvenance;
import nl.knaw.huygens.alexandria.model.TentativeAlexandriaProvenance;

public class ProvenancePrototype {
  private String who;
  private InstantParam when;
  private String why;

  private Optional<String> getWho() {
    return Optional.ofNullable(who);
  }

  public Instant getWhen() {
    return when == null ? Instant.now() : when.getValue();
  }

  private Optional<String> getWhy() {
    return Optional.ofNullable(why);
  }

  public TentativeAlexandriaProvenance getValue() {
    return new TentativeAlexandriaProvenance(//
        getWho().orElse(ThreadContext.getUserName()), //
        getWhen(),//
        getWhy().orElse(AlexandriaProvenance.DEFAULT_WHY)//
    );
  }
}
