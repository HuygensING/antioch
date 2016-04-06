package nl.knaw.huygens.alexandria.endpoint.resource;

import java.net.URI;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

import nl.knaw.huygens.alexandria.api.EndpointPaths;
import nl.knaw.huygens.alexandria.api.model.Entity;
import nl.knaw.huygens.alexandria.api.model.JsonWrapperObject;
import nl.knaw.huygens.alexandria.api.model.PropertyPrefix;
import nl.knaw.huygens.alexandria.endpoint.LocationBuilder;
import nl.knaw.huygens.alexandria.model.AlexandriaResource;

@JsonTypeName("resourceText")
@JsonInclude(Include.NON_NULL)
public class TextEntity extends JsonWrapperObject implements Entity {
  @JsonIgnore
  private LocationBuilder locationBuilder;
  @JsonIgnore
  private UUID resourceId;

  public TextEntity(UUID resourceId) {
    this.resourceId = resourceId;
  }

  public final static TextEntity of(UUID resourceId) {
    return new TextEntity(resourceId);
  }

  public final TextEntity withLocationBuilder(LocationBuilder locationBuilder) {
    this.locationBuilder = locationBuilder;
    return this;
  }

  @JsonProperty(PropertyPrefix.LINK + "baselayerDefinition")
  public URI getBaselayerDefinition() {
    return locationBuilder.locationOf(AlexandriaResource.class, resourceId, EndpointPaths.BASELAYERDEFINITION);
  }

  @JsonProperty(PropertyPrefix.LINK + "baselayer")
  public URI getBaselayer() {
    return locationBuilder.locationOf(AlexandriaResource.class, resourceId, "text", "baselayer");
  }

  @JsonProperty(PropertyPrefix.LINK + "xml")
  public URI getXml() {
    return locationBuilder.locationOf(AlexandriaResource.class, resourceId, "text", "xml");
  }

  @JsonProperty(PropertyPrefix.LINK + "dot")
  public URI getDot() {
    return locationBuilder.locationOf(AlexandriaResource.class, resourceId, "text", "dot");
  }

}
