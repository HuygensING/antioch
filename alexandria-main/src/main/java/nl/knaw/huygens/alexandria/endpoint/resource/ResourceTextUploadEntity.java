package nl.knaw.huygens.alexandria.endpoint.resource;

/*
 * #%L
 * alexandria-main
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
import nl.knaw.huygens.alexandria.api.EndpointPaths;
import nl.knaw.huygens.alexandria.api.model.Entity;
import nl.knaw.huygens.alexandria.api.model.JsonWrapperObject;
import nl.knaw.huygens.alexandria.api.model.PropertyPrefix;
import nl.knaw.huygens.alexandria.endpoint.LocationBuilder;
import nl.knaw.huygens.alexandria.model.AlexandriaResource;
import nl.knaw.huygens.alexandria.text.AnnotationData;
import nl.knaw.huygens.alexandria.text.BaseLayerData;

@JsonTypeName("resourceTextUploadResult")
@JsonInclude(Include.NON_NULL)
@ApiModel("ResourceTextUploadresult")
public class ResourceTextUploadEntity extends JsonWrapperObject implements Entity {
  @JsonIgnore
  protected LocationBuilder locationBuilder;

  @JsonIgnore
  private UUID baseLayerDefiningResourceId;

  private List<String> annotationActions;

  @JsonProperty(PropertyPrefix.LINK + "baseLayerDefinition")
  public URI getBaseLayerDefinitionURI() {
    return locationBuilder.locationOf(AlexandriaResource.class, baseLayerDefiningResourceId, EndpointPaths.BASELAYERDEFINITION);
  }

  @JsonProperty("dryrun")
  public List<String> getAnnotationActions() {
    return annotationActions;
  }

  private ResourceTextUploadEntity(UUID baseLayerDefiningResourceId, BaseLayerData baseLayerData) {
    this.baseLayerDefiningResourceId = baseLayerDefiningResourceId;
    this.annotationActions = baseLayerData.getAnnotationData().stream().map(AnnotationData::toVerbose).collect(toList());
    List<BaseLayerData> subLayerData = baseLayerData.getSubLayerData();
    handleSubLayerData(subLayerData);
  }

  private void handleSubLayerData(List<BaseLayerData> subLayerData) {
    subLayerData.forEach(sld -> {
      String xml = sld.getBaseLayer();
      this.annotationActions.add("adding subresource with text '" + xml + "'");
      this.annotationActions.addAll(sld.getAnnotationData().stream().map(AnnotationData::toVerbose).collect(toList()));
      handleSubLayerData(sld.getSubLayerData());
    });
  }

  public final ResourceTextUploadEntity withLocationBuilder(LocationBuilder locationBuilder) {
    this.locationBuilder = locationBuilder;
    return this;
  }

  public static ResourceTextUploadEntity of(UUID baseLayerDefiningResourceId, BaseLayerData baseLayerData) {
    return new ResourceTextUploadEntity(baseLayerDefiningResourceId, baseLayerData);
  }

}
