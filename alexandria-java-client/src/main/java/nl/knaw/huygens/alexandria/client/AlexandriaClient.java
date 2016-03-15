package nl.knaw.huygens.alexandria.client;

import static nl.knaw.huygens.alexandria.api.ApiConstants.HEADER_AUTH;

import java.net.URI;
import java.util.UUID;
import java.util.function.Supplier;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.jackson.JacksonFeature;

import nl.knaw.huygens.Log;
import nl.knaw.huygens.alexandria.api.EndpointPaths;
import nl.knaw.huygens.alexandria.api.model.AboutEntity;
import nl.knaw.huygens.alexandria.api.model.AlexandriaState;
import nl.knaw.huygens.alexandria.api.model.BaseLayerDefinition;
import nl.knaw.huygens.alexandria.api.model.BaseLayerDefinitionPrototype;
import nl.knaw.huygens.alexandria.api.model.StatePrototype;

public class AlexandriaClient {
  private WebTarget rootTarget;
  private String authHeader = "";
  private Client client;
  private URI alexandriaURI;
  private boolean autoConfirm = true;

  public AlexandriaClient(URI alexandriaURI) {
    this.alexandriaURI = alexandriaURI;
    client = ClientBuilder.newClient().register(JacksonFeature.class);
    client.property(ClientProperties.CONNECT_TIMEOUT, 60000);
    client.property(ClientProperties.READ_TIMEOUT, 60000);
    rootTarget = client.target(alexandriaURI);
  }

  public void setProperty(String jerseyClientProperty, Object value) {
    client.property(jerseyClientProperty, value);
    rootTarget = client.target(alexandriaURI);
  }

  public void setAuthKey(String authKey) {
    authHeader = "SimpleAuth " + authKey;
    Log.info("authheader=[{}]", authHeader);
  }

  public void setAutoConfirm(boolean autoConfirm) {
    this.autoConfirm = autoConfirm;
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
    Entity<ResourcePrototype> entity = Entity.json(resource);
    Supplier<Response> responseSupplier = () -> rootTarget//
        .path(EndpointPaths.RESOURCES)//
        .path(resourceId.toString())//
        .request()//
        .header(HEADER_AUTH, authHeader)//
        .put(entity);
    RestRequester<Void> requester = RestRequester.withResponseSupplier(responseSupplier);

    return requester//
        .onStatus(Status.CREATED, (response) -> new RestResult<>())//
        .getResult();
  }

  public RestResult<UUID> addResource(ResourcePrototype resource) {
    Entity<ResourcePrototype> entity = Entity.json(resource);
    Supplier<Response> responseSupplier = () -> rootTarget//
        .path(EndpointPaths.RESOURCES)//
        .request()//
        .header(HEADER_AUTH, authHeader)//
        .post(entity);
    RestRequester<UUID> requester = RestRequester.withResponseSupplier(responseSupplier);
    RestResult<UUID> addResult = requester//
        .onStatus(Status.CREATED, (response) -> {
          RestResult<UUID> result = new RestResult<>();
          String location = response.getHeaderString("Location");
          UUID uuid = UUID.fromString(location.replaceFirst(".*/", ""));
          result.setCargo(uuid);
          return result;
        })//
        .getResult();
    if (autoConfirm && !addResult.hasFailed()) {
      confirmResource(addResult.get());
    }

    return addResult;
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

  public RestResult<Void> confirmResource(UUID resourceUuid) {
    StatePrototype state = new StatePrototype().setState(AlexandriaState.CONFIRMED);
    Entity<StatePrototype> confirmation = Entity.json(state);
    Supplier<Response> responseSupplier = () -> rootTarget.path(EndpointPaths.RESOURCES)//
        .path(resourceUuid.toString())//
        .path("state")//
        .request()//
        .header(HEADER_AUTH, authHeader)//
        .put(confirmation);
    RestRequester<Void> requester = RestRequester.withResponseSupplier(responseSupplier);
    return requester//
        .onStatus(Status.NO_CONTENT, (response) -> new RestResult<>())//
        .getResult();
  }

  public RestResult<URI> setBaseLayerDefinition(UUID resourceUuid, BaseLayerDefinitionPrototype baselayerDefinition) {
    Entity<BaseLayerDefinitionPrototype> entity = Entity.json(baselayerDefinition);
    Supplier<Response> responseSupplier = () -> rootTarget.path(EndpointPaths.RESOURCES)//
        .path(resourceUuid.toString())//
        .path(EndpointPaths.BASELAYERDEFINITION)//
        .request()//
        .header(HEADER_AUTH, authHeader)//
        .put(entity);
    RestRequester<URI> requester = RestRequester.withResponseSupplier(responseSupplier);
    return requester//
        .onStatus(Status.CREATED, (response) -> {
          RestResult<URI> result = new RestResult<>();
          String location = response.getHeaderString("Location");
          URI uri = URI.create(location);
          result.setCargo(uri);
          return result;
        })//
        .getResult();
  }

  public RestResult<BaseLayerDefinition> getBaseLayerDefinition(UUID uuid) {
    RestRequester<BaseLayerDefinition> requester = RestRequester.withResponseSupplier(() -> rootTarget.path(EndpointPaths.RESOURCES)//
        .path(uuid.toString())//
        .path(EndpointPaths.BASELAYERDEFINITION)//
        .request().get());
    return requester//
        .onStatus(Status.OK, (response) -> {
          RestResult<BaseLayerDefinition> result = new RestResult<>();
          BaseLayerDefinition cargo = response.readEntity(BaseLayerDefinition.class);
          result.setCargo(cargo);
          return result;
        })//
        .getResult();
  }

}
