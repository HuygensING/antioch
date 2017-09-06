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
import java.util.Set;
import java.util.UUID;

import com.google.common.collect.ImmutableSet;

import nl.knaw.huygens.antioch.api.model.AntiochState;

public abstract class AbstractAccountable implements Accountable {
  private final UUID id;
  private final AntiochProvenance provenance;
  private AntiochState state = AntiochState.TENTATIVE;
  private Instant stateSince = Instant.now();

  protected AbstractAccountable(UUID id, TentativeAntiochProvenance provenance) {
    this.id = id;
    this.provenance = provenance.bind(this);
  }

  @Override
  public UUID getId() {
    return id;
  }

  @Override
  public AntiochProvenance getProvenance() {
    return provenance;
  }

  @Override
  public AntiochState getState() {
    return state;
  }

  @Override
  public Instant getStateSince() {
    return stateSince;
  }

  public void setState(AntiochState state) {
    this.state = state;
    this.stateSince = Instant.now();
  }

  final Set<AntiochState> ACTIVE_STATES = ImmutableSet.of(AntiochState.CONFIRMED, AntiochState.TENTATIVE);

  public boolean isActive() {
    return ACTIVE_STATES.contains(this.state);
  }

  // Only to be used when deframing from graphdb
  public void setStateSince(Instant instant) {
    this.stateSince = instant;
  }

}
