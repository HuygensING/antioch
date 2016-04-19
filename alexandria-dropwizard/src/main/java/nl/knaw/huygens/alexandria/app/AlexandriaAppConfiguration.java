package nl.knaw.huygens.alexandria.app;

import org.hibernate.validator.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.dropwizard.Configuration;

public class AlexandriaAppConfiguration extends Configuration {
  @NotEmpty
  private String name;

  @JsonProperty
  String getName() {
    return name;
  }

  @JsonProperty
  public void setName(String name) {
    this.name = name;
  }
}
