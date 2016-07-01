package nl.knaw.huygens.alexandria.app;

import java.net.URI;
import java.util.Map;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableMap;

import io.dropwizard.Configuration;
import nl.knaw.huygens.Log;
import nl.knaw.huygens.alexandria.config.AlexandriaConfiguration;

public class AlexandriaAppConfiguration extends Configuration implements AlexandriaConfiguration {
  @NotNull
  private URI baseURI;
  @NotEmpty
  private String adminKey;
  @NotEmpty
  private String storageDirectory;
  private Boolean asynchronousEndpointsAllowed = true;

  public AlexandriaAppConfiguration() {
    super();
    Log.info("AlexandriaAppConfiguration initialized");
  }

  @JsonProperty
  public void setBaseURI(URI baseURI) {
    this.baseURI = baseURI;
  }

  @Override
  @JsonProperty
  public URI getBaseURI() {
    return baseURI;
  }

  @JsonProperty
  public void setStorageDirectory(String storageDirectory) {
    this.storageDirectory = storageDirectory;
  }

  @Override
  @JsonProperty
  public String getStorageDirectory() {
    return storageDirectory;
  }

  @Override
  public Map<String, String> getAuthKeyIndex() {
    return ImmutableMap.of("admin", adminKey);
  }

  @JsonProperty
  public void setAdminKey(String adminKey) {
    this.adminKey = adminKey;
  }

  @Override
  @JsonProperty
  public String getAdminKey() {
    return adminKey;
  }

  @Override
  public Boolean asynchronousEndpointsAllowed() {
    return getAsynchronousEndpointsAllowed();
  }

  @JsonProperty
  public void setAsynchronousEndpointsAllowed(Boolean asynchronousEndpointsAllowed) {
    this.asynchronousEndpointsAllowed = asynchronousEndpointsAllowed;
  }

  @JsonProperty
  public Boolean getAsynchronousEndpointsAllowed() {
    return asynchronousEndpointsAllowed;
  }

}
