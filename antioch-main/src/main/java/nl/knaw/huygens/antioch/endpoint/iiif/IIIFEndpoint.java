package nl.knaw.huygens.antioch.endpoint.iiif;

/*
 * #%L
 * antioch-main
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

import java.net.URI;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import com.google.common.collect.ImmutableMap;

import io.swagger.annotations.Api;
import nl.knaw.huygens.antioch.api.EndpointPaths;
import nl.knaw.huygens.antioch.api.model.ProcessStatusMap;
import nl.knaw.huygens.antioch.config.AntiochConfiguration;
import nl.knaw.huygens.antioch.endpoint.JSONEndpoint;
import nl.knaw.huygens.antioch.endpoint.LocationBuilder;
import nl.knaw.huygens.antioch.service.AntiochService;

@Singleton
@Path(EndpointPaths.IIIF)
@Api(EndpointPaths.IIIF)
public class IIIFEndpoint extends JSONEndpoint {

  private final AntiochService service;
  private final AntiochConfiguration config;
  private final ExecutorService executorService;
  private final ProcessStatusMap<AnnotationListImportStatus> taskStatusMap;

  @Context
  UriInfo uriInfo;

  @Inject
  public IIIFEndpoint(AntiochService service, //
      LocationBuilder locationBuilder, //
      AntiochConfiguration config, //
      ProcessStatusMap<AnnotationListImportStatus> taskStatusMap,//
      ExecutorService executorService) {
    this.service = service;
    LocationBuilder locationBuilder1 = locationBuilder;
    this.config = config;
    this.taskStatusMap = taskStatusMap;
    this.executorService = executorService;
  }

  @GET
  public Response getExampleUrls() {
    Map<String, URI> exampleMap = ImmutableMap.<String, URI> builder()//
        .put("Collection", buildURI("collection", "name"))//
        .put("Manifest", buildURI("identifier", "manifest"))//
        .put("Sequence", buildURI("identifier", "sequence", "name"))//
        .put("Canvas", buildURI("identifier", "canvas", "name"))//
        .put("Annotation", buildURI("identifier", "annotation", "name"))//
        .put("AnnotationList", buildURI("identifier", "list", "name"))//
        .put("Range", buildURI("identifier", "range", "name"))//
        .put("Layer", buildURI("identifier", "layer", "name"))//
        .put("Content", buildURI("identifier", "res", "name.format"))//
        .build();
    return ok(exampleMap);
  }

  // Recommended URI Patterns

  // Collection {scheme}://{host}/{prefix}/collection/{name}
  @Path("collection/{name}")
  public IIIFCollectionEndpoint getIIIFCollectionEndpoint(@PathParam("name") String name) {
    return new IIIFCollectionEndpoint(name, service, uriInfo.getAbsolutePath());
  }

  // Manifest {scheme}://{host}/{prefix}/{identifier}/manifest
  @Path("{identifier}/manifest")
  public IIIFManifestEndpoint getIIIFManifestEndpoint(@PathParam("identifier") String identifier) {
    return new IIIFManifestEndpoint(identifier, service, uriInfo.getAbsolutePath());
  }

  // Sequence {scheme}://{host}/{prefix}/{identifier}/sequence/{name}
  @Path("{identifier}/sequence/{name}")
  public IIIFSequenceEndpoint getIIIFSequenceEndpoint(@PathParam("identifier") String identifier, @PathParam("name") String name) {
    return new IIIFSequenceEndpoint(identifier, name, service, uriInfo.getAbsolutePath());
  }

  // Canvas {scheme}://{host}/{prefix}/{identifier}/canvas/{name}
  @Path("{identifier}/canvas/{name}")
  public IIIFCanvasEndpoint getIIIFCanvasEndpoint(@PathParam("identifier") String identifier, @PathParam("name") String name) {
    return new IIIFCanvasEndpoint(identifier, name, service, uriInfo.getAbsolutePath());
  }

  // Annotation {scheme}://{host}/{prefix}/{identifier}/annotation/{name}
  @Path("{identifier}/annotation/{name}")
  public IIIFAnnotationEndpoint getIIIFAnnotationEndpoint(@PathParam("identifier") String identifier, @PathParam("name") String name) {
    return new IIIFAnnotationEndpoint(identifier, name, service, uriInfo.getAbsolutePath());
  }

  // AnnotationList {scheme}://{host}/{prefix}/{identifier}/list/{name}
  @Path("{identifier}/list/{name}")
  public IIIFAnnotationListEndpoint getIIIFAnnotationListEndpoint(@PathParam("identifier") String identifier, @PathParam("name") String name) {
    return new IIIFAnnotationListEndpoint(identifier, name, service, config, uriInfo.getAbsolutePath(), taskStatusMap, executorService);
  }

  // Range {scheme}://{host}/{prefix}/{identifier}/range/{name}
  @Path("{identifier}/range/{name}")
  public IIIFRangeEndpoint getIIIFRangeEndpoint(@PathParam("identifier") String identifier, @PathParam("name") String name) {
    return new IIIFRangeEndpoint(identifier, name, service, uriInfo.getAbsolutePath());
  }

  // Layer {scheme}://{host}/{prefix}/{identifier}/layer/{name}
  @Path("{identifier}/layer/{name}")
  public IIIFLayerEndpoint getIIIFLayerEndpoint(@PathParam("identifier") String identifier, @PathParam("name") String name) {
    return new IIIFLayerEndpoint(identifier, name, service, uriInfo.getAbsolutePath());
  }

  // Content {scheme}://{host}/{prefix}/{identifier}/res/{name}.{format}
  @Path("{identifier}/res/{name}.{format}")
  public IIIFContentEndpoint getIIIFContentEndpoint(@PathParam("identifier") String identifier, @PathParam("name") String name, @PathParam("format") String format) {
    return new IIIFContentEndpoint(identifier, name, format, service, uriInfo.getAbsolutePath());
  }

  private URI buildURI(String... paths) {
    UriBuilder uriBuilder = uriInfo.getAbsolutePathBuilder();
    for (String path : paths) {
      uriBuilder.path(path);
    }
    return uriBuilder.build();
  }
}
