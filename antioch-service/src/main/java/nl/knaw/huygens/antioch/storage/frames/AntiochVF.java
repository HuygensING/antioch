package nl.knaw.huygens.antioch.storage.frames;

/*
 * #%L
 * antioch-service
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

import nl.knaw.huygens.antioch.api.model.AntiochState;

public abstract class AntiochVF extends IdentifiableVF {

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
    return hasState(AntiochState.DEPRECATED);
  }

  public boolean isDeleted() {
    return hasState(AntiochState.DELETED);
  }

  public boolean isConfirmed() {
    return hasState(AntiochState.CONFIRMED);
  }

  public boolean isTentative() {
    return hasState(AntiochState.TENTATIVE);
  }

  private boolean hasState(AntiochState state) {
    return state.name().equals(getState());
  }
}
