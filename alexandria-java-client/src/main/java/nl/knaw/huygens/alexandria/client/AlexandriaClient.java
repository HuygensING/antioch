package nl.knaw.huygens.alexandria.client;

import java.net.URI;
import java.util.UUID;
import java.util.function.Supplier;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.glassfish.jersey.jackson.JacksonFeature;

import nl.knaw.huygens.Log;
import nl.knaw.huygens.alexandria.api.EndpointPaths;
import nl.knaw.huygens.alexandria.api.model.AboutEntity;

public class AlexandriaClient {
  private WebTarget rootTarget;
  private String authHeader = "";

  public AlexandriaClient(URI alexandriaURI) {
    Client client = ClientBuilder.newClient().register(JacksonFeature.class);
    rootTarget = client.target(alexandriaURI);
  }

  public void setAuthKey(String authKey) {
    authHeader = "SimpleAuth " + authKey;
    Log.info("authheader=[{}]", authHeader);
  }

  public RestResult<AboutEntity> getAbout() {
    RestRequester<AboutEntity> requester = RestRequester.withResponseSupplier(() -> rootTarget.path(EndpointPaths.ABOUT).request().get());
    return requester//
        .onStatus(Status.OK, (response) -> {
          RestResult<AboutEntity> result = new RestResult<>();
          AboutEntity cargo = response.readEntity(AboutEntity.class);
          result.setCargo(cargo);
          return result;
        })//
        .getResult();
  }

  public RestResult<Void> setResource(UUID resourceId, ResourcePrototype resource) {
    Entity<ResourcePrototype> entity = Entity.entity(resource, MediaType.APPLICATION_JSON);
    Supplier<Response> responseSupplier = () -> rootTarget//
        .path(EndpointPaths.RESOURCES)//
        .path(resourceId.toString())//
        .request()//
        .header("Auth", authHeader)//
        .put(entity);
    RestRequester<Void> requester = RestRequester.withResponseSupplier(responseSupplier);

    return requester//
        .onStatus(Status.CREATED, (response) -> {
          return new RestResult<>();
        })//
        .getResult();
  }

  public RestResult<UUID> addResource(ResourcePrototype resource) {
    Entity<ResourcePrototype> entity = Entity.entity(resource, MediaType.APPLICATION_JSON);
    Supplier<Response> responseSupplier = () -> rootTarget//
        .path(EndpointPaths.RESOURCES)//
        .request()//
        .header("Auth", authHeader)//
        .post(entity);
    RestRequester<UUID> requester = RestRequester.withResponseSupplier(responseSupplier);
    return requester//
        .onStatus(Status.CREATED, (response) -> {
          RestResult<UUID> result = new RestResult<>();
          String location = response.getHeaderString("Location");
          UUID uuid = UUID.fromString(location.replaceFirst(".*/", ""));
          result.setCargo(uuid);
          return result;
        })//
        .getResult();
  }

  public RestResult<ResourceEntity> getResource(UUID uuid) {
    RestRequester<ResourceEntity> requester = RestRequester.withResponseSupplier(() -> rootTarget.path(EndpointPaths.RESOURCES).path(uuid.toString()).request().get());
    return requester//
        .onStatus(Status.OK, (response) -> {
          RestResult<ResourceEntity> result = new RestResult<>();
          ResourceEntity cargo = response.readEntity(ResourceEntity.class);
          result.setCargo(cargo);
          return result;
        })//
        .getResult();
  }

}
