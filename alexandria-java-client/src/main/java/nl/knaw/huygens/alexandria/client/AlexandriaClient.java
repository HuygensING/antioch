package nl.knaw.huygens.alexandria.client;

/*
 * #%L
 * alexandria-java-client
 * =======
 * Copyright (C) 2015 - 2016 Huygens ING (KNAW)
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

import static nl.knaw.huygens.alexandria.api.ApiConstants.HEADER_AUTH;

import java.net.URI;
import java.util.UUID;
import java.util.function.Supplier;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.SyncInvoker;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;

import nl.knaw.huygens.Log;
import nl.knaw.huygens.alexandria.api.EndpointPaths;
import nl.knaw.huygens.alexandria.api.model.AboutEntity;
import nl.knaw.huygens.alexandria.api.model.AlexandriaState;
import nl.knaw.huygens.alexandria.api.model.StatePrototype;
import nl.knaw.huygens.alexandria.api.model.TextEntity;
import nl.knaw.huygens.alexandria.api.model.TextImportStatus;
import nl.knaw.huygens.alexandria.api.model.TextView;
import nl.knaw.huygens.alexandria.api.model.TextViewDefinition;
import nl.knaw.huygens.alexandria.client.model.AnnotationPojo;
import nl.knaw.huygens.alexandria.client.model.AnnotationPrototype;
import nl.knaw.huygens.alexandria.client.model.ResourcePojo;
import nl.knaw.huygens.alexandria.client.model.ResourcePrototype;
import nl.knaw.huygens.alexandria.client.model.SubResourcePojo;
import nl.knaw.huygens.alexandria.client.model.SubResourcePrototype;

public class AlexandriaClient {
  private WebTarget rootTarget;
  private String authHeader = "";
  private final Client client;
  private final URI alexandriaURI;
  private boolean autoConfirm = true;

  public AlexandriaClient(final URI alexandriaURI) {
    this.alexandriaURI = alexandriaURI;
    final ObjectMapper objectMapper = new ObjectMapper()//
        .registerModule(new Jdk8Module())//
        .registerModule(new JavaTimeModule());
    final JacksonJaxbJsonProvider jacksonProvider = new JacksonJaxbJsonProvider();
    jacksonProvider.setMapper(objectMapper);
    client = ClientBuilder.newClient(new ClientConfig(jacksonProvider));
    client.property(ClientProperties.CONNECT_TIMEOUT, 60000);
    client.property(ClientProperties.READ_TIMEOUT, 60000);
    rootTarget = client.target(alexandriaURI);
  }

  public void setProperty(final String jerseyClientProperty, final Object value) {
    client.property(jerseyClientProperty, value);
    rootTarget = client.target(alexandriaURI);
  }

  public void setAuthKey(final String authKey) {
    authHeader = "SimpleAuth " + authKey;
    Log.info("authheader=[{}]", authHeader);
  }

  public void setAutoConfirm(final boolean autoConfirm) {
    this.autoConfirm = autoConfirm;
  }

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
        .onStatus(Status.CREATED, (response) -> new RestResult<>())//
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

  public RestResult<Void> confirmResource(final UUID resourceUUID) {
    return confirm(EndpointPaths.RESOURCES, resourceUUID);
  }

  public RestResult<Void> confirmAnnotation(final UUID annotationUuid) {
    return confirm(EndpointPaths.ANNOTATIONS, annotationUuid);
  }

  public RestResult<URI> setResourceText(final UUID resourceUUID, final String xml) {
    final Entity<String> entity = Entity.entity(xml, MediaType.TEXT_XML);
    WebTarget path = resourceTextTarget(resourceUUID);
    final Supplier<Response> responseSupplier = authorizedPut(path, entity);
    final RestRequester<URI> requester = RestRequester.withResponseSupplier(responseSupplier);
    return requester//
        .onStatus(Status.ACCEPTED, this::uriFromLocationHeader)//
        .getResult();
  }

  public RestResult<TextImportStatus> getTextImportStatus(final UUID resourceUUID) {
    final WebTarget path = resourceTextTarget(resourceUUID)//
        .path("status");
    final Supplier<Response> responseSupplier = anonymousGet(path);
    final RestRequester<TextImportStatus> requester = RestRequester.withResponseSupplier(responseSupplier);
    return requester//
        .onStatus(Status.OK, this::toTextImportStatusRestResult)//
        .getResult();
  }

  public RestResult<TextEntity> getTextInfo(UUID resourceUUID) {
    WebTarget path = resourceTextTarget(resourceUUID);
    final Supplier<Response> responseSupplier = anonymousGet(path);
    final RestRequester<TextEntity> requester = RestRequester.withResponseSupplier(responseSupplier);
    return requester//
        .onStatus(Status.OK, this::toTextEntityRestResult)//
        .getResult();
  }

  public RestResult<String> getTextAsString(final UUID uuid) {
    WebTarget path = resourceTextTarget(uuid).path("xml");
    return stringResult(path);
  }

  public RestResult<String> getTextAsString(final UUID uuid, final String viewName) {
    WebTarget path = resourceTextTarget(uuid).path("xml").queryParam("view", viewName);
    return stringResult(path);
  }

  public RestResult<String> getTextAsDot(final UUID uuid) {
    WebTarget path = resourceTextTarget(uuid).path("dot");
    return stringResult(path);
  }

  public RestResult<URI> setResourceTextView(final UUID resourceUUID, final String textViewName, final TextViewDefinition textView) {
    final Entity<TextViewDefinition> entity = Entity.json(textView);
    final WebTarget path = resourceTextTarget(resourceUUID)//
        .path(EndpointPaths.TEXTVIEWS)//
        .path(textViewName);
    final Supplier<Response> responseSupplier = authorizedPut(path, entity);
    final RestRequester<URI> requester = RestRequester.withResponseSupplier(responseSupplier);
    return requester//
        .onStatus(Status.CREATED, this::uriFromLocationHeader)//
        .getResult();
  }

  public RestResult<TextView> getTextView(final UUID uuid) {
    WebTarget path = resourceTextTarget(uuid).path(EndpointPaths.TEXTVIEWS);
    Supplier<Response> anonymousGet = anonymousGet(path);
    final RestRequester<TextView> requester = RestRequester.withResponseSupplier(anonymousGet);
    return requester//
        .onStatus(Status.OK, this::toTextViewRestResult)//
        .getResult();
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

  private WebTarget resourceTextTarget(final UUID resourceUUID) {
    return resourceTarget(resourceUUID).path(EndpointPaths.TEXT);
  }

  private WebTarget resourceTarget(final UUID uuid) {
    return rootTarget//
        .path(EndpointPaths.RESOURCES)//
        .path(uuid.toString());
  }

  private RestResult<Void> confirm(final String endpoint, final UUID resourceUUID) {
    final StatePrototype state = new StatePrototype().setState(AlexandriaState.CONFIRMED);
    final Entity<StatePrototype> confirmation = Entity.json(state);
    final WebTarget path = rootTarget//
        .path(endpoint)//
        .path(resourceUUID.toString())//
        .path("state");
    final Supplier<Response> responseSupplier = authorizedPut(path, confirmation);
    final RestRequester<Void> requester = RestRequester.withResponseSupplier(responseSupplier);
    return requester//
        .onStatus(Status.NO_CONTENT, (response) -> new RestResult<>())//
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

  private RestResult<TextImportStatus> toTextImportStatusRestResult(final Response response) {
    return toEntityRestResult(response, TextImportStatus.class);
  }

  private RestResult<TextEntity> toTextEntityRestResult(final Response response) {
    return toEntityRestResult(response, TextEntity.class);
  }

  private RestResult<TextView> toTextViewRestResult(final Response response) {
    return toEntityRestResult(response, TextView.class);
  }

  private <E> RestResult<E> toEntityRestResult(final Response response, final Class<E> entityClass) {
    final RestResult<E> result = new RestResult<>();
    final E cargo = response.readEntity(entityClass);
    result.setCargo(cargo);
    return result;
  }

  private RestResult<URI> uriFromLocationHeader(final Response response) {
    final RestResult<URI> result = new RestResult<>();
    final String location = response.getHeaderString("Location");
    final URI uri = URI.create(location);
    result.setCargo(uri);
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

  private SyncInvoker authorizedRequest(final WebTarget target) {
    return target.request()//
        .header(HEADER_AUTH, authHeader);
  }

  private RestResult<String> stringResult(WebTarget path) {
    Supplier<Response> responseSupplier = anonymousGet(path);
    final RestRequester<String> requester = RestRequester.withResponseSupplier(responseSupplier);
    return requester//
        .onStatus(Status.OK, this::toStringRestResult)//
        .getResult();
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

}
