package nl.knaw.huygens.alexandria.api.model;

/*
 * #%L
 * alexandria-api
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

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.DurationSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.InstantSerializer;

@JsonInclude(Include.NON_NULL)
public abstract class ProcessStatus extends JsonWrapperObject implements Entity {
  enum State {
    waiting, processing, done
  }

  private boolean done = false;
  private State state = State.waiting;
  private Duration processingTime;
  private Instant startTime;
  private Instant expires;

  public State getState() {
    return state;
  }

  public void setStarted() {
    this.state = State.processing;
    this.startTime = Instant.now();
  }

  public void setDone() {
    this.done = true;
    this.state = State.done;
    this.processingTime = Duration.between(startTime, Instant.now());
    this.expires = Instant.now().plus(1L, ChronoUnit.HOURS);
  }

  public boolean isDone() {
    return done;
  }

  @JsonIgnore
  public boolean isExpired() {
    return expires != null && Instant.now().isAfter(expires);
  }

  @JsonSerialize(using = InstantSerializer.class)
  public Instant getExpires() {
    return expires;
  }

  @JsonSerialize(using = InstantSerializer.class)
  public Instant getStartTime() {
    return startTime;
  }

  @JsonSerialize(using = DurationSerializer.class)
  public Duration getProcessingTime() {
    return processingTime;
  }

}
