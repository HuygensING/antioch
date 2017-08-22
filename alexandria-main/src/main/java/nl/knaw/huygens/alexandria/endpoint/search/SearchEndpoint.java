package nl.knaw.huygens.alexandria.endpoint.search;

/*
 * #%L
 * alexandria-main
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

import static nl.knaw.huygens.alexandria.api.EndpointPaths.SEARCHES;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import nl.knaw.huygens.alexandria.api.EndpointPaths;
import nl.knaw.huygens.alexandria.api.model.search.AlexandriaQuery;
import nl.knaw.huygens.alexandria.api.model.search.SearchResultPage;
import nl.knaw.huygens.alexandria.endpoint.JSONEndpoint;
import nl.knaw.huygens.alexandria.endpoint.LocationBuilder;
import nl.knaw.huygens.alexandria.endpoint.UUIDParam;
import nl.knaw.huygens.alexandria.exception.NotFoundException;
import nl.knaw.huygens.alexandria.service.AlexandriaService;

@Singleton
@Path(SEARCHES)
@Api(SEARCHES)
public class SearchEndpoint extends JSONEndpoint {

  private final AlexandriaService service;
  private final LocationBuilder locationBuilder;
  private final SearchFactory searchFactory;

  @Inject
  public SearchEndpoint(final AlexandriaService service, //
      final SearchFactory searchFactory, //
      final LocationBuilder locationBuilder) {
    this.service = service;
    this.searchFactory = searchFactory;
    this.locationBuilder = locationBuilder;
  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @ApiOperation("create new SearchResult")
  public Response createSearchResult(@NotNull @Valid AlexandriaQuery query) {
    // Log.trace("query=[{}]", query);
    SearchResult searchResult = service.execute(query);
    SearchResultCache.add(searchResult);
    return created(locationBuilder.locationOf(searchResult));
  }

  @GET
  @Path("{uuid}")
  @ApiOperation(value = "Get the SearchResult with the given uuid", response = SearchResult.class)
  public Response getSearchResultsByID(@PathParam("uuid") final UUIDParam uuid) {
    return getResultPage(uuid, 1);
  }

  @GET
  @Path("{uuid}/" + EndpointPaths.RESULTPAGES + "/{pageNumber:[0-9]+}")
  @ApiOperation(value = "Get the SearchResultPage with the given uuid and page number", response = SearchResultPage.class)
  public Response getResultPage(@PathParam("uuid") final UUIDParam uuid, @PathParam("pageNumber") int pageNumber) {
    SearchResult searchResult = getSearchResult(uuid);
    int totalResultPages = Math.max(1, searchResult.getTotalPages());
    // if (totalResultPages == 0) {
    // throw new NotFoundException("no result pages found");
    // }
    if (pageNumber < 1 || pageNumber > totalResultPages) {
      throw new NotFoundException("pageNumber should be between 1 and " + totalResultPages);
    }
    String baseURI = locationBuilder.locationOf(searchResult, EndpointPaths.RESULTPAGES) + "/";
    SearchResultPage page = searchFactory.createSearchResultPage(baseURI, pageNumber, searchResult, searchResult.getRecordsForPage(pageNumber));
    return ok(page);
  }

  private SearchResult getSearchResult(final UUIDParam uuid) {
    return SearchResultCache.get(uuid.getValue()) //
        .orElseThrow(() -> new NotFoundException("no SearchResult found with id " + uuid));
  }

}
