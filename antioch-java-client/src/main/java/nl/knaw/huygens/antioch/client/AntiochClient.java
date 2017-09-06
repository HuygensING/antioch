package nl.knaw.huygens.antioch.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import static nl.knaw.huygens.antioch.api.ApiConstants.HEADER_AUTH;
import nl.knaw.huygens.antioch.api.EndpointPaths;
import nl.knaw.huygens.antioch.api.model.AboutEntity;
import nl.knaw.huygens.antioch.api.model.AntiochState;
import nl.knaw.huygens.antioch.api.model.StatePrototype;
import nl.knaw.huygens.antioch.api.model.search.AntiochQuery;
import nl.knaw.huygens.antioch.api.model.search.SearchResultPage;
import nl.knaw.huygens.antioch.client.model.*;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.glassfish.jersey.apache.connector.ApacheClientProperties;
import org.glassfish.jersey.apache.connector.ApacheConnectorProvider;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;

import javax.net.ssl.SSLContext;
import javax.ws.rs.client.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.net.URI;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

/*
 * #%L
 * antioch-java-client
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

public class AntiochClient implements AutoCloseable {
  public static final Map<String, String> NO_VIEW_PARAMETERS = Collections.emptyMap();

  private WebTarget rootTarget;
  private String authHeader = "";
  private final Client client;
  private final URI antiochURI;
  private boolean autoConfirm = true;

  public AntiochClient(final URI antiochURI) {
    this(antiochURI, null);
  }

  public AntiochClient(final URI antiochURI, SSLContext sslContext) {
    this.antiochURI = antiochURI;
    final ObjectMapper objectMapper = new ObjectMapper()//
        .registerModule(new Jdk8Module())//
        .registerModule(new JavaTimeModule());

    final JacksonJaxbJsonProvider jacksonProvider = new JacksonJaxbJsonProvider();
    jacksonProvider.setMapper(objectMapper);

    PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
    cm.setMaxTotal(50);
    cm.setDefaultMaxPerRoute(50);

    ApacheConnectorProvider connectorProvider = new ApacheConnectorProvider();
    ClientConfig clientConfig = new ClientConfig(jacksonProvider)//
        .connectorProvider(connectorProvider)//
        .property(ApacheClientProperties.CONNECTION_MANAGER, cm)//
        .property(ClientProperties.CONNECT_TIMEOUT, 600)//
        .property(ClientProperties.READ_TIMEOUT, 600);

    if (sslContext == null) {
      if ("https".equals(antiochURI.getScheme())) {
        throw new RuntimeException("SSL connections need an SSLContext, use: new AntiochClient(uri, sslContext) instead.");
      }
      client = ClientBuilder.newClient(clientConfig);

    } else {
      client = ClientBuilder.newBuilder()//
          .sslContext(sslContext)//
          .withConfig(clientConfig)//
          .build();
    }
    rootTarget = client.target(antiochURI);
  }

  @Override
  public void close() {
    client.close();
  }

  public void register(Object component) {
    client.register(component);
    rootTarget = client.target(antiochURI);
  }

  public void setProperty(final String jerseyClientProperty, final Object value) {
    client.property(jerseyClientProperty, value);
    rootTarget = client.target(antiochURI);
  }

  public void setAuthKey(final String authKey) {
    authHeader = "SimpleAuth " + authKey;
  }

  /**
   * When autoConfirm is true (default), a resource/annotation made with POST will be automatically confirmed.
   *
   * @param autoConfirm
   */
  public void setAutoConfirm(final boolean autoConfirm) {
    this.autoConfirm = autoConfirm;
  }

  // Antioch API methods

  public RestResult<AboutEntity> getAbout() {
    WebTarget path = rootTarget//
        .path(EndpointPaths.ABOUT);
    Supplier<Response> responseSupplier = anonymousGet(path);
    final RestRequester<AboutEntity> requester = RestRequester.withResponseSupplier(responseSupplier);
    return requester//
        .onStatus(Status.OK, this::toAboutEntityRestResult)//
        .getResult();
  }

  public RestResult<Void> setResource(final UUID resourceId, final ResourcePrototype resource) {
    final WebTarget path = resourceTarget(resourceId);
    final Entity<ResourcePrototype> entity = Entity.json(resource);
    final Supplier<Response> responseSupplier = authorizedPut(path, entity);
    final RestRequester<Void> requester = RestRequester.withResponseSupplier(responseSupplier);

    return requester//
        .onStatus(Status.CREATED, voidRestResult())//
        .onStatus(Status.ACCEPTED, voidRestResult())//
        .onStatus(Status.NO_CONTENT, voidRestResult())//
        .getResult();
  }

  public RestResult<UUID> addResource(final ResourcePrototype resource) {
    final WebTarget path = rootTarget.path(EndpointPaths.RESOURCES);
    final Entity<ResourcePrototype> entity = Entity.json(resource);
    final Supplier<Response> responseSupplier = authorizedPost(path, entity);
    final RestRequester<UUID> requester = RestRequester.withResponseSupplier(responseSupplier);
    final RestResult<UUID> addResult = requester//
        .onStatus(Status.CREATED, this::uuidFromLocationHeader)//
        .getResult();
    if (autoConfirm && !addResult.hasFailed()) {
      confirmResource(addResult.get());
    }

    return addResult;
  }

  public RestResult<UUID> addSubResource(final UUID parentResourceId, final SubResourcePrototype subresource) {
    final Entity<SubResourcePrototype> entity = Entity.json(subresource);
    final WebTarget path = resourceTarget(parentResourceId)//
        .path(EndpointPaths.SUBRESOURCES);
    final Supplier<Response> responseSupplier = authorizedPost(path, entity);
    final RestRequester<UUID> requester = RestRequester.withResponseSupplier(responseSupplier);
    final RestResult<UUID> addResult = requester//
        .onStatus(Status.CREATED, this::uuidFromLocationHeader)//
        .getResult();
    if (autoConfirm && !addResult.hasFailed()) {
      confirmResource(addResult.get());
    }
    return addResult;
  }

  public RestResult<Void> setSubResource(final UUID parentResourceId, final UUID subResourceId, final SubResourcePrototype subresource) {
    final Entity<SubResourcePrototype> entity = Entity.json(subresource);
    final WebTarget path = resourceTarget(parentResourceId)//
        .path(EndpointPaths.SUBRESOURCES)//
        .path(subResourceId.toString());
    final Supplier<Response> responseSupplier = authorizedPut(path, entity);
    final RestRequester<Void> requester = RestRequester.withResponseSupplier(responseSupplier);
    return requester//
        .onStatus(Status.CREATED, voidRestResult())//
        .onStatus(Status.NO_CONTENT, voidRestResult())//
        .getResult();
  }

  public RestResult<ResourcePojo> getResource(final UUID uuid) {
    WebTarget path = resourceTarget(uuid);
    Supplier<Response> responseSupplier = anonymousGet(path);
    final RestRequester<ResourcePojo> requester = RestRequester.withResponseSupplier(responseSupplier);
    return requester//
        .onStatus(Status.OK, this::toResourcePojoRestResult)//
        .getResult();
  }

  public RestResult<SubResourcePojo> getSubResource(final UUID uuid) {
    WebTarget path = resourceTarget(uuid);
    Supplier<Response> responseSupplier = anonymousGet(path);
    final RestRequester<SubResourcePojo> requester = RestRequester.withResponseSupplier(responseSupplier);
    return requester//
        .onStatus(Status.OK, this::toSubResourcePojoRestResult)//
        .getResult();
  }

  public RestResult<AnnotationList> getResourceAnnotations(final UUID resourceUUID) {
    WebTarget path = resourceTarget(resourceUUID).path(EndpointPaths.ANNOTATIONS);
    final RestRequester<AnnotationList> requester = RestRequester.withResponseSupplier(anonymousGet(path));
    return requester//
        .onStatus(Status.OK, this::toAnnotationListRestResult)//
        .getResult();
  }

  public RestResult<SubResourceList> getSubResources(final UUID resourceUUID) {
    WebTarget path = resourceTarget(resourceUUID).path(EndpointPaths.SUBRESOURCES);
    final RestRequester<SubResourceList> requester = RestRequester.withResponseSupplier(anonymousGet(path));
    return requester//
        .onStatus(Status.OK, this::toSubResourceListRestResult)//
        .getResult();
  }

  public RestResult<Void> confirmResource(final UUID resourceUUID) {
    return confirm(EndpointPaths.RESOURCES, resourceUUID);
  }

  public RestResult<Void> confirmAnnotation(final UUID annotationUuid) {
    return confirm(EndpointPaths.ANNOTATIONS, annotationUuid);
  }

  public RestResult<UUID> annotateResource(final UUID resourceUUID, final AnnotationPrototype annotationPrototype) {
    return annotate(resourceUUID, annotationPrototype, EndpointPaths.RESOURCES);
  }

  public RestResult<UUID> annotateAnnotation(final UUID annotationUuid, final AnnotationPrototype annotationPrototype) {
    return annotate(annotationUuid, annotationPrototype, EndpointPaths.ANNOTATIONS);
  }

  public RestResult<AnnotationPojo> getAnnotation(final UUID uuid) {
    WebTarget path = annotationTarget(uuid);
    return getAnnotationRestResult(path);
  }

  public RestResult<AnnotationPojo> getAnnotationRevision(final UUID uuid, final Integer revision) {
    WebTarget path = annotationTarget(uuid).path(EndpointPaths.REV).path(revision.toString());
    return getAnnotationRestResult(path);
  }

  public RestResult<AnnotationList> getAnnotationAnnotations(final UUID annotationUUID) {
    WebTarget path = annotationTarget(annotationUUID).path(EndpointPaths.ANNOTATIONS);
    final RestRequester<AnnotationList> requester = RestRequester.withResponseSupplier(anonymousGet(path));
    return requester//
        .onStatus(Status.OK, this::toAnnotationListRestResult)//
        .getResult();
  }

  public RestResult<UUID> addSearch(AntiochQuery query) {
    final Entity<AntiochQuery> entity = Entity.json(query);
    final WebTarget path = rootTarget.path(EndpointPaths.SEARCHES);
    final Supplier<Response> responseSupplier = authorizedPost(path, entity);
    final RestRequester<UUID> requester = RestRequester.withResponseSupplier(responseSupplier);
    return requester//
        .onStatus(Status.CREATED, this::uuidFromLocationHeader)//
        .getResult();
  }

  public RestResult<SearchResultPage> getSearchResultPage(UUID searchId) {
    return getSearchResultPage(searchId, 1);
  }

  public RestResult<SearchResultPage> getSearchResultPage(UUID searchId, Integer page) {
    final WebTarget path = rootTarget//
        .path(EndpointPaths.SEARCHES)//
        .path(searchId.toString())//
        .path(EndpointPaths.RESULTPAGES)//
        .path(page.toString());
    final RestRequester<SearchResultPage> requester = RestRequester.withResponseSupplier(anonymousGet(path));
    return requester//
        .onStatus(Status.OK, this::toSearchResultPageRestResult)//
        .getResult();
  }

  public RestResult<Void> updateAnnotation(final UUID annotatableUuid, final AnnotationPrototype annotationPrototype) {
    WebTarget path = annotationTarget(annotatableUuid);
    final Entity<AnnotationPrototype> entity = Entity.json(annotationPrototype);
    final RestRequester<Void> requester = RestRequester.withResponseSupplier(authorizedPut(path, entity));
    return requester//
        .onStatus(Status.NO_CONTENT, voidRestResult())//
        .getResult();
  }

  public RestResult<Void> deprecateAnnotation(UUID uuid) {
    WebTarget path = annotationTarget(uuid);
    final RestRequester<Void> requester = RestRequester.withResponseSupplier(authorizedDelete(path));
    return requester//
        .onStatus(Status.NO_CONTENT, voidRestResult())//
        .getResult();
  }

  // private methods

  private RestResult<UUID> annotate(final UUID annotatableUuid, final AnnotationPrototype annotationPrototype, final String annotatablePath) {
    final Entity<AnnotationPrototype> entity = Entity.json(annotationPrototype);
    final WebTarget path = rootTarget//
        .path(annotatablePath)//
        .path(annotatableUuid.toString())//
        .path(EndpointPaths.ANNOTATIONS);
    final Supplier<Response> responseSupplier = authorizedPost(path, entity);

    final RestRequester<UUID> requester = RestRequester.withResponseSupplier(responseSupplier);
    final RestResult<UUID> annotateResult = requester//
        .onStatus(Status.CREATED, this::uuidFromLocationHeader)//
        .getResult();
    if (autoConfirm && !annotateResult.hasFailed()) {
      confirmAnnotation(annotateResult.get());
    }
    return annotateResult;
  }

  private WebTarget resourceTarget(final UUID uuid) {
    return rootTarget//
        .path(EndpointPaths.RESOURCES)//
        .path(uuid.toString());
  }

  private RestResult<Void> confirm(final String endpoint, final UUID resourceUUID) {
    final StatePrototype state = new StatePrototype().setState(AntiochState.CONFIRMED);
    final Entity<StatePrototype> confirmation = Entity.json(state);
    final WebTarget path = rootTarget//
        .path(endpoint)//
        .path(resourceUUID.toString())//
        .path("state");
    final Supplier<Response> responseSupplier = authorizedPut(path, confirmation);
    final RestRequester<Void> requester = RestRequester.withResponseSupplier(responseSupplier);
    return requester//
        .onStatus(Status.NO_CONTENT, voidRestResult())//
        .getResult();
  }

  private RestResult<String> toStringRestResult(final Response response) {
    return toEntityRestResult(response, String.class);
  }

  private RestResult<AboutEntity> toAboutEntityRestResult(final Response response) {
    return toEntityRestResult(response, AboutEntity.class);
  }

  private RestResult<ResourcePojo> toResourcePojoRestResult(final Response response) {
    return toEntityRestResult(response, ResourcePojo.class);
  }

  private RestResult<SubResourcePojo> toSubResourcePojoRestResult(final Response response) {
    return toEntityRestResult(response, SubResourcePojo.class);
  }


  private RestResult<AnnotationPojo> toAnnotationPojoRestResult(final Response response) {
    return toEntityRestResult(response, AnnotationPojo.class);
  }


  private RestResult<SearchResultPage> toSearchResultPageRestResult(final Response response) {
    return toEntityRestResult(response, SearchResultPage.class);
  }


  private RestResult<AnnotationList> toAnnotationListRestResult(Response response) {
    return toEntityRestResult(response, AnnotationList.class);
  }

  private RestResult<SubResourceList> toSubResourceListRestResult(Response response) {
    return toEntityRestResult(response, SubResourceList.class);
  }

  private <E> RestResult<E> toEntityRestResult(final Response response, final Class<E> entityClass) {
    final RestResult<E> result = new RestResult<>();
    final E cargo = response.readEntity(entityClass);
    result.setCargo(cargo);
    return result;
  }

  private RestResult<UUID> uuidFromLocationHeader(final Response response) {
    final RestResult<UUID> result = new RestResult<>();
    final String location = response.getHeaderString("Location");
    final UUID uuid = UUID.fromString(location.replaceFirst(".*/", ""));
    result.setCargo(uuid);
    return result;
  }

  private Supplier<Response> anonymousGet(final WebTarget target) {
    return () -> target.request().get();
  }

  private Supplier<Response> authorizedPut(final WebTarget path, final Entity<?> entity) {
    return () -> authorizedRequest(path).put(entity);
  }

  private Supplier<Response> authorizedPost(final WebTarget path, final Entity<?> entity) {
    return () -> authorizedRequest(path).post(entity);
  }

  private Supplier<Response> authorizedDelete(final WebTarget path) {
    return () -> authorizedRequest(path).delete();
  }

  private SyncInvoker authorizedRequest(final WebTarget target) {
    return target.request()//
        .accept(MediaType.APPLICATION_JSON_TYPE)//
        .header(HEADER_AUTH, authHeader);
  }

  private WebTarget annotationTarget(final UUID uuid) {
    return rootTarget.path(EndpointPaths.ANNOTATIONS).path(uuid.toString());
  }

  private RestResult<AnnotationPojo> getAnnotationRestResult(WebTarget path) {
    final RestRequester<AnnotationPojo> requester = RestRequester.withResponseSupplier(anonymousGet(path));
    return requester//
        .onStatus(Status.OK, this::toAnnotationPojoRestResult)//
        .getResult();
  }

  private Function<Response, RestResult<Void>> voidRestResult() {
    return (response) -> {
      response.bufferEntity(); // to notify connectors, such as the ApacheConnector, that the entity has been "consumed" and that it should release the current connection back into the Apache
      // ConnectionManager pool (if being used). https://java.net/jira/browse/JERSEY-3149
      return new RestResult<>();
    };
  }

  public WebTarget getRootTarget() {
    return rootTarget;
  }
}
