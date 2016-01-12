package nl.knaw.huygens.alexandria.endpoint;

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

import java.time.Instant;
import java.util.Optional;

import nl.knaw.huygens.alexandria.jaxrs.ThreadContext;
import nl.knaw.huygens.alexandria.model.AlexandriaProvenance;
import nl.knaw.huygens.alexandria.model.TentativeAlexandriaProvenance;

public class ProvenancePrototype {
  private String who;
  private InstantParam when;
  private String why;

  public Optional<String> getWho() {
    return Optional.ofNullable(who);
  }

  public Instant getWhen() {
    return when == null ? Instant.now() : when.getValue();
  }

  public Optional<String> getWhy() {
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
