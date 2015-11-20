package nl.knaw.huygens.alexandria.endpoint.search;

/*
 * #%L
 * alexandria-main
 * =======
 * Copyright (C) 2015 Huygens ING (KNAW)
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

import static nl.knaw.huygens.alexandria.endpoint.EndpointPaths.SEARCHES;

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
import nl.knaw.huygens.Log;
import nl.knaw.huygens.alexandria.endpoint.JSONEndpoint;
import nl.knaw.huygens.alexandria.endpoint.LocationBuilder;
import nl.knaw.huygens.alexandria.endpoint.UUIDParam;
import nl.knaw.huygens.alexandria.exception.NotFoundException;
import nl.knaw.huygens.alexandria.model.search.AlexandriaQuery;
import nl.knaw.huygens.alexandria.service.AlexandriaService;

@Singleton
@Path(SEARCHES)
@Api(SEARCHES)
public class SearchEndpoint extends JSONEndpoint {

  private final AlexandriaService service;
  private final SearchResultEntityBuilder entityBuilder;
  private final LocationBuilder locationBuilder;

  @Inject
  public SearchEndpoint(final AlexandriaService service, //
      final SearchResultEntityBuilder entityBuilder, //
      final LocationBuilder locationBuilder) {
    this.service = service;
    this.entityBuilder = entityBuilder;
    this.locationBuilder = locationBuilder;
  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @ApiOperation("create new SearchResult")
  public Response createSearchResult(@NotNull @Valid AlexandriaQuery query) {
    Log.trace("query=[{}]", query);
    SearchResult searchResult = service.execute(query);
    SearchResultCache.add(searchResult);
    return Response.created(locationBuilder.locationOf(searchResult)).build();
  }

  @GET
  @Path("{uuid}")
  @ApiOperation(value = "Get the SearchResult with the given uuid", response = SearchResult.class)
  public Response getSearchResultsByID(@PathParam("uuid") final UUIDParam uuid) {
    SearchResult search = getSearchResult(uuid);
    return Response.ok(entityBuilder.build(search)).build();
  }

  public static final String RESULTPAGES = "pages";

  @GET
  @Path("{uuid}/" + RESULTPAGES + "/{pageNumber:[0-9]+}")
  @ApiOperation(value = "Get the SearchResultPage with the given uuid and page number", response = SearchResultPage.class)
  public Response getResultPage(@PathParam("uuid") final UUIDParam uuid, @PathParam("pageNumber") int pageNumber) {
    SearchResult searchResult = getSearchResult(uuid);
    int totalResultPages = searchResult.getTotalPages();
    if (totalResultPages == 0) {
      throw new NotFoundException("no result pages found");
    }
    if (pageNumber < 1 || pageNumber > totalResultPages) {
      throw new NotFoundException("pageNumber should be between 1 and " + totalResultPages);
    }
    String baseURI = locationBuilder.locationOf(searchResult) + "/" + RESULTPAGES + "/";
    SearchResultPage page = new SearchResultPage(baseURI, pageNumber, totalResultPages, searchResult.getPageSize());
    page.setResults(searchResult.getRecordsForPage(pageNumber));
    return Response.ok(page).build();
  }

  private SearchResult getSearchResult(final UUIDParam uuid) {
    return SearchResultCache.get(uuid.getValue()) //
        .orElseThrow(() -> new NotFoundException("no SearchResult found with id " + uuid));
  }

}
