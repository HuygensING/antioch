package nl.knaw.huygens.alexandria.api.model;

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
