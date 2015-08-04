package nl.knaw.huygens.alexandria.endpoint.search;

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
import nl.knaw.huygens.alexandria.service.AlexandriaService;

@Singleton
@Path(SEARCHES)
@Api(SEARCHES)
public class SearchEndpoint extends JSONEndpoint {

  private final AlexandriaService service;
  private final SearchResultEntityBuilder entityBuilder;
  private final LocationBuilder locationBuilder;

  @Inject
  public SearchEndpoint(final AlexandriaService service,                                   //
      final SearchResultEntityBuilder entityBuilder,                                  //
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
  @ApiOperation(value = "Get the SearchResult with the given uuid", response = SearchEntity.class)
  public Response getSearchResultsByID(@PathParam("uuid") final UUIDParam uuid) {
    SearchResult search = SearchResultCache.get(uuid.getValue())//
        .orElseThrow(() -> new NotFoundException("no SearchResult found with id " + uuid));
    return Response.ok(entityBuilder.build(search)).build();
  }

}
