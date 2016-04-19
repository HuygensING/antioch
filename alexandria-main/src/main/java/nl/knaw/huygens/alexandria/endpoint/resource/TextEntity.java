package nl.knaw.huygens.alexandria.endpoint.resource;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.google.common.collect.Lists;

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
  private final LocationBuilder locationBuilder;
  @JsonIgnore
  private final UUID resourceId;
  @JsonIgnore
  private List<TextViewEntity> textViews = Lists.newArrayList();

  public TextEntity(UUID resourceId, LocationBuilder locationBuilder) {
    this.resourceId = resourceId;
    this.locationBuilder = locationBuilder;
    textViews.add(new TextViewEntity(resourceId, "baselayer", locationBuilder));
  }

  @JsonProperty("views")
  public List<TextViewEntity> getTextViews() {
    return textViews;
  }

  @JsonProperty(PropertyPrefix.LINK + "xml")
  public URI getXml() {
    return locationBuilder.locationOf(AlexandriaResource.class, resourceId, EndpointPaths.TEXT, "xml");
  }

  @JsonProperty(PropertyPrefix.LINK + "dot")
  public URI getDot() {
    return locationBuilder.locationOf(AlexandriaResource.class, resourceId, EndpointPaths.TEXT, "dot");
  }

}
