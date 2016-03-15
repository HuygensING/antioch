package nl.knaw.huygens.alexandria.api.model;

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

import com.fasterxml.jackson.annotation.JsonIgnore;

public class StatePrototype {
  private AlexandriaState state;

  public AlexandriaState getState() {
    return state;
  }

  public StatePrototype setState(AlexandriaState state) {
    this.state = state;
    return this;
  }

  @JsonIgnore
  public boolean isConfirmed() {
    return AlexandriaState.CONFIRMED.equals(state);
  }
}
