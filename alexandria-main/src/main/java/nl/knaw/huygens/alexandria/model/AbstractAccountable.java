package nl.knaw.huygens.alexandria.model;

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
import java.util.Set;
import java.util.UUID;

import com.google.common.collect.ImmutableSet;

import nl.knaw.huygens.alexandria.api.model.AlexandriaState;

public abstract class AbstractAccountable implements Accountable {
  private final UUID id;
  private final AlexandriaProvenance provenance;
  private AlexandriaState state = AlexandriaState.TENTATIVE;
  private Instant stateSince = Instant.now();

  protected AbstractAccountable(UUID id, TentativeAlexandriaProvenance provenance) {
    this.id = id;
    this.provenance = provenance.bind(this);
  }

  @Override
  public UUID getId() {
    return id;
  }

  @Override
  public AlexandriaProvenance getProvenance() {
    return provenance;
  }

  @Override
  public AlexandriaState getState() {
    return state;
  }

  @Override
  public Instant getStateSince() {
    return stateSince;
  }

  public void setState(AlexandriaState state) {
    this.state = state;
    this.stateSince = Instant.now();
  }

  Set<AlexandriaState> ACTIVE_STATES = ImmutableSet.of(AlexandriaState.CONFIRMED, AlexandriaState.TENTATIVE);

  public boolean isActive() {
    return ACTIVE_STATES.contains(this.state);
  }

  // Only to be used when deframing from graphdb
  public void setStateSince(Instant instant) {
    this.stateSince = instant;
  }

}
