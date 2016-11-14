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

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.net.ssl.SSLContext;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.SyncInvoker;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.io.FileUtils;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;

import nl.knaw.huygens.alexandria.api.EndpointPaths;
import nl.knaw.huygens.alexandria.api.model.AboutEntity;
import nl.knaw.huygens.alexandria.api.model.AlexandriaState;
import nl.knaw.huygens.alexandria.api.model.Annotator;
import nl.knaw.huygens.alexandria.api.model.AnnotatorList;
import nl.knaw.huygens.alexandria.api.model.CommandResponse;
import nl.knaw.huygens.alexandria.api.model.CommandStatus;
import nl.knaw.huygens.alexandria.api.model.StatePrototype;
import nl.knaw.huygens.alexandria.api.model.search.AlexandriaQuery;
import nl.knaw.huygens.alexandria.api.model.search.SearchResultPage;
import nl.knaw.huygens.alexandria.api.model.text.TextEntity;
import nl.knaw.huygens.alexandria.api.model.text.TextImportStatus;
import nl.knaw.huygens.alexandria.api.model.text.TextRangeAnnotation;
import nl.knaw.huygens.alexandria.api.model.text.TextRangeAnnotationInfo;
import nl.knaw.huygens.alexandria.api.model.text.TextRangeAnnotationList;
import nl.knaw.huygens.alexandria.api.model.text.view.TextView;
import nl.knaw.huygens.alexandria.api.model.text.view.TextViewDefinition;
import nl.knaw.huygens.alexandria.client.model.AnnotationList;
import nl.knaw.huygens.alexandria.client.model.AnnotationPojo;
import nl.knaw.huygens.alexandria.client.model.AnnotationPrototype;
import nl.knaw.huygens.alexandria.client.model.ResourcePojo;
import nl.knaw.huygens.alexandria.client.model.ResourcePrototype;
import nl.knaw.huygens.alexandria.client.model.SubResourceList;
import nl.knaw.huygens.alexandria.client.model.SubResourcePojo;
import nl.knaw.huygens.alexandria.client.model.SubResourcePrototype;

public class AlexandriaClient implements AutoCloseable {
  private WebTarget rootTarget;
  private String authHeader = "";
  private final Client client;
  private final URI alexandriaURI;
  private boolean autoConfirm = true;

  public AlexandriaClient(final URI alexandriaURI) {
    this(alexandriaURI, null);
  }

  public AlexandriaClient(final URI alexandriaURI, SSLContext sslContext) {
    this.alexandriaURI = alexandriaURI;
    final ObjectMapper objectMapper = new ObjectMapper()//
        .registerModule(new Jdk8Module())//
        .registerModule(new JavaTimeModule());
    final JacksonJaxbJsonProvider jacksonProvider = new JacksonJaxbJsonProvider();
    jacksonProvider.setMapper(objectMapper);
    if (sslContext == null) {
      if ("https".equals(alexandriaURI.getScheme())) {
        throw new RuntimeException("SSL connections need an SSLContext, use: new AlexandriaClient(uri, sslContext) instead.");
      }
      client = ClientBuilder.newClient(new ClientConfig(jacksonProvider));

    } else {
      client = ClientBuilder.newBuilder()//
          .sslContext(sslContext)//
          .withConfig(new ClientConfig(jacksonProvider))//
          .build();
    }
    client.property(ClientProperties.CONNECT_TIMEOUT, 60000);
    client.property(ClientProperties.READ_TIMEOUT, 60000);
    rootTarget = client.target(alexandriaURI);
  }

  @Override
  public void close() {
    client.close();
  }

  public void register(Object component) {
    client.register(component);
    rootTarget = client.target(alexandriaURI);
  }

  public void setProperty(final String jerseyClientProperty, final Object value) {
    client.property(jerseyClientProperty, value);
    rootTarget = client.target(alexandriaURI);
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

  // Alexandria API methods

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

  public RestResult<Void> setAnnotator(UUID resourceUUID, String code, Annotator annotator) {
    final Entity<Annotator> entity = Entity.json(annotator);
    final WebTarget path = annotatorsTarget(resourceUUID, code);
    final Supplier<Response> responseSupplier = authorizedPut(path, entity);
    final RestRequester<Void> requester = RestRequester.withResponseSupplier(responseSupplier);
    return requester//
        .onStatus(Status.CREATED, voidRestResult())//
        .onStatus(Status.NO_CONTENT, voidRestResult())//
        .getResult();
  }

  public RestResult<Annotator> getAnnotator(UUID resourceUUID, String code) {
    final WebTarget path = annotatorsTarget(resourceUUID, code);
    final Supplier<Response> responseSupplier = anonymousGet(path);
    final RestRequester<Annotator> requester = RestRequester.withResponseSupplier(responseSupplier);
    return requester//
        .onStatus(Status.OK, this::toAnnotatorRestResult)//
        .getResult();
  }

  public RestResult<AnnotatorList> getAnnotators(UUID resourceUUID) {
    final WebTarget path = resourceTarget(resourceUUID)//
        .path(EndpointPaths.ANNOTATORS);

    final Supplier<Response> responseSupplier = anonymousGet(path);
    final RestRequester<AnnotatorList> requester = RestRequester.withResponseSupplier(responseSupplier);
    return requester//
        .onStatus(Status.OK, this::toAnnotatorListRestResult)//
        .getResult();
  }

  public RestResult<Void> setResourceText(final UUID resourceUUID, final File file) throws IOException {
    return setResourceText(resourceUUID, FileUtils.readFileToString(file, StandardCharsets.UTF_8));
  }

  public RestResult<Void> setResourceText(final UUID resourceUUID, final String xml) {
    final Entity<String> entity = Entity.entity(xml, MediaType.TEXT_XML);
    WebTarget path = resourceTextTarget(resourceUUID);
    final Supplier<Response> responseSupplier = authorizedPut(path, entity);
    final RestRequester<Void> requester = RestRequester.withResponseSupplier(responseSupplier);
    return requester//
        .onStatus(Status.ACCEPTED, voidRestResult())//
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

  public RestResult<TextRangeAnnotationInfo> setResourceTextRangeAnnotation(UUID resourceUUID, TextRangeAnnotation textAnnotation) {
    final Entity<TextRangeAnnotation> entity = Entity.json(textAnnotation);
    WebTarget path = resourceTextTarget(resourceUUID)//
        .path(EndpointPaths.ANNOTATIONS)//
        .path(textAnnotation.getId().toString());
    final Supplier<Response> responseSupplier = authorizedPut(path, entity);
    final RestRequester<TextRangeAnnotationInfo> requester = RestRequester.withResponseSupplier(responseSupplier);
    return requester//
        .onStatus(Status.CREATED, this::toTextRangeAnnotationInfoRestResult)//
        .onStatus(Status.NO_CONTENT, this::toTextRangeAnnotationInfoRestResult)//
        .getResult();
  }

  public RestResult<TextRangeAnnotation> getResourceTextRangeAnnotation(UUID resourceUUID, UUID annotationUUID) {
    WebTarget path = resourceTextTarget(resourceUUID)//
        .path(EndpointPaths.ANNOTATIONS)//
        .path(annotationUUID.toString());
    final Supplier<Response> responseSupplier = anonymousGet(path);
    final RestRequester<TextRangeAnnotation> requester = RestRequester.withResponseSupplier(responseSupplier);
    return requester//
        .onStatus(Status.OK, this::toTextRangeAnnotationRestResult)//
        .getResult();
  }

  public RestResult<TextRangeAnnotationList> getResourceTextRangeAnnotations(UUID resourceUUID) {
    WebTarget path = resourceTextTarget(resourceUUID)//
        .path(EndpointPaths.ANNOTATIONS);
    final Supplier<Response> responseSupplier = anonymousGet(path);
    final RestRequester<TextRangeAnnotationList> requester = RestRequester.withResponseSupplier(responseSupplier);
    return requester//
        .onStatus(Status.OK, this::toTextRangeAnnotationListRestResult)//
        .getResult();
  }

  public RestResult<Void> setResourceTextView(final UUID resourceUUID, final String textViewName, final TextViewDefinition textView) {
    final Entity<TextViewDefinition> entity = Entity.json(textView);
    final WebTarget path = resourceTextTarget(resourceUUID)//
        .path(EndpointPaths.TEXTVIEWS)//
        .path(textViewName);
    final Supplier<Response> responseSupplier = authorizedPut(path, entity);
    final RestRequester<Void> requester = RestRequester.withResponseSupplier(responseSupplier);
    return requester//
        .onStatus(Status.CREATED, voidRestResult())//
        .getResult();
  }

  public RestResult<TextView> getResourceTextView(final UUID uuid) {
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

  public RestResult<AnnotationList> getAnnotationAnnotations(final UUID annotationUUID) {
    WebTarget path = annotationTarget(annotationUUID).path(EndpointPaths.ANNOTATIONS);
    final RestRequester<AnnotationList> requester = RestRequester.withResponseSupplier(anonymousGet(path));
    return requester//
        .onStatus(Status.OK, this::toAnnotationListRestResult)//
        .getResult();
  }

  public RestResult<UUID> addSearch(AlexandriaQuery query) {
    final Entity<AlexandriaQuery> entity = Entity.json(query);
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

  public RestResult<CommandResponse> doCommand(String commandName, Map<String, Object> parameters) {
    final Entity<Map<String, Object>> entity = Entity.json(parameters);
    final WebTarget path = rootTarget.path(EndpointPaths.COMMANDS).path(commandName);
    final Supplier<Response> responseSupplier = authorizedPost(path, entity);
    final RestRequester<CommandResponse> requester = RestRequester.withResponseSupplier(responseSupplier);
    return requester//
        .onStatus(Status.OK, this::toCommandResponseRestResult)//
        .onStatus(Status.ACCEPTED, this::extractCommandStatusId)//
        .getResult();
  }

  public RestResult<CommandStatus> getCommandStatus(final String commandName, final UUID resourceUUID) {
    final WebTarget path = rootTarget.path(EndpointPaths.COMMANDS)//
        .path(commandName)//
        .path(resourceUUID.toString())//
        .path("status");
    final Supplier<Response> responseSupplier = anonymousGet(path);
    final RestRequester<CommandStatus> requester = RestRequester.withResponseSupplier(responseSupplier);
    return requester//
        .onStatus(Status.OK, this::toCommandStatusRestResult)//
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

  private RestResult<Annotator> toAnnotatorRestResult(final Response response) {
    return toEntityRestResult(response, Annotator.class);
  }

  private RestResult<AnnotatorList> toAnnotatorListRestResult(final Response response) {
    return toEntityRestResult(response, AnnotatorList.class);
  }

  private RestResult<AnnotationPojo> toAnnotationPojoRestResult(final Response response) {
    return toEntityRestResult(response, AnnotationPojo.class);
  }

  private RestResult<TextImportStatus> toTextImportStatusRestResult(final Response response) {
    return toEntityRestResult(response, TextImportStatus.class);
  }

  private RestResult<CommandStatus> toCommandStatusRestResult(final Response response) {
    return toEntityRestResult(response, CommandStatus.class);
  }

  private RestResult<TextEntity> toTextEntityRestResult(final Response response) {
    return toEntityRestResult(response, TextEntity.class);
  }

  private RestResult<TextView> toTextViewRestResult(final Response response) {
    return toEntityRestResult(response, TextView.class);
  }

  private RestResult<SearchResultPage> toSearchResultPageRestResult(final Response response) {
    return toEntityRestResult(response, SearchResultPage.class);
  }

  private RestResult<CommandResponse> toCommandResponseRestResult(final Response response) {
    return toEntityRestResult(response, CommandResponse.class);
  }

  private RestResult<TextRangeAnnotation> toTextRangeAnnotationRestResult(final Response response) {
    return toEntityRestResult(response, TextRangeAnnotation.class);
  }

  private RestResult<TextRangeAnnotationList> toTextRangeAnnotationListRestResult(final Response response) {
    return toEntityRestResult(response, TextRangeAnnotationList.class);
  }

  private RestResult<TextRangeAnnotationInfo> toTextRangeAnnotationInfoRestResult(final Response response) {
    return toEntityRestResult(response, TextRangeAnnotationInfo.class);
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

  private RestResult<CommandResponse> extractCommandStatusId(final Response response) {
    final String location = response.getHeaderString("Location");
    String[] parts = location.split("/");
    UUID statusId = UUID.fromString(parts[parts.length - 2]);
    CommandResponse commandResponse = new CommandResponse();
    commandResponse.setStatusId(statusId);
    final RestResult<CommandResponse> result = new RestResult<>();
    return result.setCargo(commandResponse);
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

  private Supplier<Response> authorizedDelete(final WebTarget path) {
    return () -> authorizedRequest(path).delete();
  }

  private SyncInvoker authorizedRequest(final WebTarget target) {
    return target.request()//
        .accept(MediaType.APPLICATION_JSON_TYPE)//
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

  private WebTarget annotatorsTarget(UUID resourceUUID, String code) {
    return resourceTarget(resourceUUID)//
        .path(EndpointPaths.ANNOTATORS)//
        .path(code);
  }

  private Function<Response, RestResult<Void>> voidRestResult() {
    return (response) -> new RestResult<>();
  }

}
