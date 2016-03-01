package nl.knaw.huygens.alexandria.endpoint.resource;

import static java.util.stream.Collectors.toList;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

import io.swagger.annotations.ApiModel;
import nl.knaw.huygens.alexandria.api.model.JsonWrapperObject;
import nl.knaw.huygens.alexandria.api.model.PropertyPrefix;
import nl.knaw.huygens.alexandria.endpoint.Entity;
import nl.knaw.huygens.alexandria.endpoint.LocationBuilder;
import nl.knaw.huygens.alexandria.model.AlexandriaResource;
import nl.knaw.huygens.alexandria.text.AnnotationData;

@JsonTypeName("resourceTextUploadResult")
@JsonInclude(Include.NON_NULL)
@ApiModel("ResourceTextUploadresult")
public class ResourceTextUploadEntity extends JsonWrapperObject implements Entity {
  @JsonIgnore
  protected LocationBuilder locationBuilder;

  @JsonIgnore
  private UUID baseLayerDefiningResourceId;

  private List<String> annotationActions;

  @JsonProperty(PropertyPrefix.LINK + "generatedAnnotations")
  private List<URI> generatedAnnotations;

  @JsonProperty(PropertyPrefix.LINK + "baseLayerDefinition")
  public URI getBaseLayerDefinitionURI() {
    return locationBuilder.locationOf(AlexandriaResource.class, baseLayerDefiningResourceId, ResourcesEndpoint.BASELAYERDEFINITION);
  }

  @JsonProperty("dryrun")
  public List<String> getAnnotationActions() {
    return annotationActions;
  }

  private ResourceTextUploadEntity(UUID baseLayerDefiningResourceId, List<AnnotationData> annotationData) {
    this.baseLayerDefiningResourceId = baseLayerDefiningResourceId;
    this.annotationActions = annotationData.stream().map(AnnotationData::toVerbose).collect(toList());
  }

  public final ResourceTextUploadEntity withLocationBuilder(LocationBuilder locationBuilder) {
    this.locationBuilder = locationBuilder;
    return this;
  }

  public static ResourceTextUploadEntity of(UUID baseLayerDefiningResourceId, List<AnnotationData> annotationData) {
    return new ResourceTextUploadEntity(baseLayerDefiningResourceId, annotationData);
  }

  public ResourceTextUploadEntity withGeneratedAnnotations(List<URI> generatedAnnotations) {
    this.generatedAnnotations = generatedAnnotations;
    return this;
  }

}
