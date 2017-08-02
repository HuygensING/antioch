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

  final Set<AlexandriaState> ACTIVE_STATES = ImmutableSet.of(AlexandriaState.CONFIRMED, AlexandriaState.TENTATIVE);

  public boolean isActive() {
    return ACTIVE_STATES.contains(this.state);
  }

  // Only to be used when deframing from graphdb
  public void setStateSince(Instant instant) {
    this.stateSince = instant;
  }

}
