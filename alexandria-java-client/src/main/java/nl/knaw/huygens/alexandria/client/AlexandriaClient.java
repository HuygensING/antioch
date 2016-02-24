package nl.knaw.huygens.alexandria.client;

import java.net.URI;
import java.util.UUID;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.jackson.JacksonFeature;

import nl.knaw.huygens.alexandria.api.EndpointPaths;
import nl.knaw.huygens.alexandria.api.model.AboutEntity;

public class AlexandriaClient {
  private WebTarget rootTarget;
  private String authHeader = "";

  public AlexandriaClient(URI alexandriaURI) {
    Client client = ClientBuilder.newClient().register(JacksonFeature.class);
    rootTarget = client.target(alexandriaURI);
  }

  public AboutEntity getAbout() {
    Response response = rootTarget.path(EndpointPaths.ABOUT).request().get();
    return response.readEntity(AboutEntity.class);
  }

  public void setAuthKey(String authKey) {
    authHeader = "SimpleAuth " + authKey;
  }

  public UUID addResource(ResourcePrototype resource) {
    Entity<ResourcePrototype> entity = Entity.entity(resource, MediaType.APPLICATION_JSON);
    Response response = rootTarget.path(EndpointPaths.RESOURCES).request()//
        .header("Auth", authHeader)//
        .post(entity);

    String location = response.getHeaderString("Location");
    return UUID.fromString(location.replaceFirst(".*/", ""));
  }

}
