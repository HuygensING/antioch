package nl.knaw.huygens.alexandria.client;

import java.net.URI;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.jackson.JacksonFeature;

import nl.knaw.huygens.alexandria.model.AboutEntity;

public class AlexandriaClient {
  private WebTarget rootTarget;

  public AlexandriaClient(URI alexandriaURI) {
    Client client = ClientBuilder.newClient().register(JacksonFeature.class);
    rootTarget = client.target(alexandriaURI);
  }

  public AboutEntity getAbout() {
    Response response = rootTarget.path("about").request().get();
    return response.readEntity(AboutEntity.class);
  }

}
