package nl.knaw.huygens.alexandria.client;

/*
 * #%L
 * alexandria-java-client
 * =======
 * Copyright (C) 2015 - 2017 Huygens ING (KNAW)
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

import nl.knaw.huygens.alexandria.api.model.AboutEntity;
import nl.knaw.huygens.alexandria.api.model.CommandResponse;
import nl.knaw.huygens.alexandria.api.model.CommandStatus;
import nl.knaw.huygens.alexandria.api.model.search.AlexandriaQuery;
import nl.knaw.huygens.alexandria.api.model.search.SearchResultPage;
import nl.knaw.huygens.alexandria.client.model.*;

import javax.net.ssl.SSLContext;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class OptimisticAlexandriaClient {
  AlexandriaClient delegate;

  // constructors

  public OptimisticAlexandriaClient(final URI alexandriaURI) {
    delegate = new AlexandriaClient(alexandriaURI);
  }

  public OptimisticAlexandriaClient(final String alexandriaURI) {
    this(URI.create(alexandriaURI));
  }

  public OptimisticAlexandriaClient(final URI alexandriaURI, SSLContext sslContext) {
    delegate = new AlexandriaClient(alexandriaURI, sslContext);
  }

  public OptimisticAlexandriaClient(final String alexandriaURI, SSLContext sslContext) {
    this(URI.create(alexandriaURI), sslContext);
  }

  // convenience methods

  public UUID addResource(String ref) {
    return addResource(resourceWithRef(ref));
  }

  public void setResource(UUID resourceId, String ref) {
    setResource(resourceId, resourceWithRef(ref));
  }

  public UUID addSubResource(UUID parentResourceId, String sub) {
    return addSubResource(parentResourceId, subResourceWithSub(sub));
  }

  public void setSubResource(UUID parentResourceId, UUID subResourceId, String sub) {
    setSubResource(parentResourceId, subResourceId, subResourceWithSub(sub));
  }

  public WebTarget getRootTarget(){
    return delegate.getRootTarget();
  }

  // delegated methods

  public void close() {
    delegate.close();
  }

  public void setProperty(String jerseyClientProperty, Object value) {
    delegate.setProperty(jerseyClientProperty, value);
  }

  public void setAuthKey(String authKey) {
    delegate.setAuthKey(authKey);
  }

  public void setAutoConfirm(boolean autoConfirm) {
    delegate.setAutoConfirm(autoConfirm);
  }

  public AboutEntity getAbout() {
    return unwrap(delegate.getAbout());
  }

  public UUID addResource(ResourcePrototype resource) {
    return unwrap(delegate.addResource(resource));
  }

  public void setResource(UUID resourceId, ResourcePrototype resource) {
    unwrap(delegate.setResource(resourceId, resource));
  }

  public UUID addSubResource(UUID parentResourceId, SubResourcePrototype subresource) {
    return unwrap(delegate.addSubResource(parentResourceId, subresource));
  }

  public void setSubResource(UUID parentResourceId, UUID subResourceId, SubResourcePrototype subresource) {
    unwrap(delegate.setSubResource(parentResourceId, subResourceId, subresource));
  }

  public ResourcePojo getResource(UUID uuid) {
    return unwrap(delegate.getResource(uuid));
  }

  public SubResourcePojo getSubResource(UUID uuid) {
    return unwrap(delegate.getSubResource(uuid));
  }

  public void confirmResource(UUID resourceUUID) {
    unwrap(delegate.confirmResource(resourceUUID));
  }

  public void confirmAnnotation(UUID annotationUuid) {
    unwrap(delegate.confirmAnnotation(annotationUuid));
  }

  public UUID annotateResource(UUID resourceUUID, AnnotationPrototype annotationPrototype) {
    return unwrap(delegate.annotateResource(resourceUUID, annotationPrototype));
  }

  public UUID annotateAnnotation(UUID annotationUuid, AnnotationPrototype annotationPrototype) {
    return unwrap(delegate.annotateAnnotation(annotationUuid, annotationPrototype));
  }

  public AnnotationPojo getAnnotation(UUID uuid) {
    return unwrap(delegate.getAnnotation(uuid));
  }

  public AnnotationPojo getAnnotationRevision(UUID uuid, Integer revision) {
    return unwrap(delegate.getAnnotationRevision(uuid, revision));
  }

  public UUID addSearch(AlexandriaQuery query) {
    return unwrap(delegate.addSearch(query));
  }

  public SearchResultPage getSearchResultPage(UUID uuid) {
    return unwrap(delegate.getSearchResultPage(uuid));
  }

  public SearchResultPage getSearchResultPage(UUID searchId, Integer page) {
    return unwrap(delegate.getSearchResultPage(searchId, page));
  }

  public CommandResponse doCommand(String commandName, Map<String, Object> parameters) {
    return unwrap(delegate.doCommand(commandName, parameters));
  }

  public CommandStatus getCommandStatus(String string, UUID uuid) {
    return unwrap(delegate.getCommandStatus(string, uuid));
  }

  public AnnotationList getResourceAnnotations(UUID uuid) {
    return unwrap(delegate.getResourceAnnotations(uuid));
  }

  public SubResourceList getSubResources(UUID uuid) {
    return unwrap(delegate.getSubResources(uuid));
  }

  public AnnotationList getAnnotationAnnotations(UUID uuid) {
    return unwrap(delegate.getAnnotationAnnotations(uuid));
  }

  public void deprecateAnnotation(UUID uuid) {
    unwrap(delegate.deprecateAnnotation(uuid));
  }

  /////// end delegated methods

  private <T> T unwrap(RestResult<T> restResult) {
    if (restResult.hasFailed()) {
      Optional<Response> response = restResult.getResponse();
      String status = response.isPresent() ? response.get().getStatus() + ": " : "";
      String message = status + restResult.getFailureCause().orElse("Unspecified error");
      throw new AlexandriaException(message);
    }
    return restResult.get();
  }

  private ResourcePrototype resourceWithRef(String ref) {
    return new ResourcePrototype().setRef(ref);
  }

  private SubResourcePrototype subResourceWithSub(String sub) {
    return new SubResourcePrototype().setSub(sub);
  }

}
