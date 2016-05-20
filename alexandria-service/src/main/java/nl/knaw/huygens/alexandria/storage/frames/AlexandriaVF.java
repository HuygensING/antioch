package nl.knaw.huygens.alexandria.storage.frames;

/*
 * #%L
 * alexandria-service
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

import nl.knaw.huygens.alexandria.api.model.AlexandriaState;

public abstract class AlexandriaVF extends VF {

  public abstract String getUuid();

  public abstract void setUuid(String uuidString);

  public abstract String getState();

  public abstract void setState(String state);

  public abstract Long getStateSince();

  public abstract void setStateSince(Long epochSecond);

  public abstract String getProvenanceWho();

  public abstract void setProvenanceWho(String who);

  public abstract String getProvenanceWhen();

  public abstract void setProvenanceWhen(String epochSecond);

  public abstract String getProvenanceWhy();

  public abstract void setProvenanceWhy(String why);

  public boolean isDeprecated() {
    return hasState(AlexandriaState.DEPRECATED);
  }

  public boolean isDeleted() {
    return hasState(AlexandriaState.DELETED);
  }

  public boolean isConfirmed() {
    return hasState(AlexandriaState.CONFIRMED);
  }

  public boolean isTentative() {
    return hasState(AlexandriaState.TENTATIVE);
  }

  private boolean hasState(AlexandriaState state) {
    return state.name().equals(getState());
  }
}
