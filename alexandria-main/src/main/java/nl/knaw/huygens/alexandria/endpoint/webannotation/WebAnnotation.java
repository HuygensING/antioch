package nl.knaw.huygens.alexandria.endpoint.webannotation;

import java.util.UUID;

public class WebAnnotation {

  private String json = "";
  private String eTag = "";
  private UUID uuid;

  public WebAnnotation(UUID id) {
    this.uuid = id;
  }

  public WebAnnotation setJson(String json) {
    this.json = json;
    return this;
  }

  public String json() {
    return json;
  }

  public WebAnnotation setETag(String eTag) {
    this.eTag = eTag;
    return this;
  }

  public String eTag() {
    return eTag;
  }

  public UUID getId() {
    return uuid;
  }



}
