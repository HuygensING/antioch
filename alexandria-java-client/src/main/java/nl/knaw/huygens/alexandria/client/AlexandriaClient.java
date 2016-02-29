package nl.knaw.huygens.alexandria.client;

import java.net.URI;
import java.util.UUID;
import java.util.function.Function;
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

  public RestResult<AboutEntity> getAbout() {
    RestRequester<AboutEntity> requester = RestRequester.withResponseSupplier(() -> rootTarget.path(EndpointPaths.ABOUT).request().get());
    return requester//
        .onOK((response) -> {
          RestResult<AboutEntity> result = new RestResult<>();
          AboutEntity cargo = response.readEntity(AboutEntity.class);
          result.setCargo(cargo);
          return result;
        })//
        .getResult();
  }

  public void setAuthKey(String authKey) {
    authHeader = "SimpleAuth " + authKey;
    Log.info("authheader=[{}]", authHeader);
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
        .onCreated((response) -> {
          RestResult<UUID> result = new RestResult<>();
          if (response.getStatusInfo().getStatusCode() == Status.CREATED.getStatusCode()) {
            String location = response.getHeaderString("Location");
            UUID uuid = UUID.fromString(location.replaceFirst(".*/", ""));
            result.setCargo(uuid);
          } else {
            result.setFail(true);
          }
          return result;
        })//
        .getResult();
  }

  public <T extends Object> RestResult<T> process(Supplier<Response> responseSupplier, Function<Response, RestResult<T>> responseMapper) {
    RestResult<T> result = new RestResult<>();
    try {
      Response response = responseSupplier.get();
      result = responseMapper.apply(response);
    } catch (Exception e) {
      e.printStackTrace();
      result.setFail(true);
    }
    return result;
  }

}
