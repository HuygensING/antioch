package nl.knaw.huygens.alexandria.client;

/*
 * #%L
 * alexandria-java-client
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

import java.net.URI;
import java.util.Optional;
import java.util.UUID;

import javax.net.ssl.SSLContext;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import nl.knaw.huygens.alexandria.api.model.AboutEntity;
import nl.knaw.huygens.alexandria.api.model.search.AlexandriaQuery;
import nl.knaw.huygens.alexandria.api.model.search.SearchResultPage;
import nl.knaw.huygens.alexandria.client.model.AnnotationList;
import nl.knaw.huygens.alexandria.client.model.AnnotationPojo;
import nl.knaw.huygens.alexandria.client.model.AnnotationPrototype;
import nl.knaw.huygens.alexandria.client.model.ResourcePojo;
import nl.knaw.huygens.alexandria.client.model.ResourcePrototype;
import nl.knaw.huygens.alexandria.client.model.SubResourceList;
import nl.knaw.huygens.alexandria.client.model.SubResourcePojo;
import nl.knaw.huygens.alexandria.client.model.SubResourcePrototype;

public class OptimisticAlexandriaClient {
  final AlexandriaClient delegate;

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

  public WebTarget getRootTarget() {
    return delegate.getRootTarget();
  }

  // TODO: move to markup module
  // public TextImportStatus setResourceTextSynchronously(UUID resourceUUID, File file) throws IOException {
  // unwrap(delegate.setResourceText(resourceUUID, file));
  // return textImportStatusWhenFinished(resourceUUID);
  // }
  //
  // public TextImportStatus setResourceTextSynchronously(UUID resourceUUID, String xml) {
  // unwrap(delegate.setResourceText(resourceUUID, xml));
  // return textImportStatusWhenFinished(resourceUUID);
  // }
  //
  // public TextAnnotationImportStatus addResourceTextRangeAnnotationsSynchronously(UUID resourceUUID, TextRangeAnnotationList textAnnotations) {
  // unwrap(delegate.addResourceTextRangeAnnotations(resourceUUID, textAnnotations));
  // TextAnnotationImportStatus finalStatus = textAnnotationImportStatusWhenFinished(resourceUUID);
  // // // throw exception when there are errors.
  // if (finalStatus.hasErrors()) {
  // throw new AlexandriaException(finalStatus.getBreakingErrorMessage());
  // }
  //
  // return finalStatus;
  // }


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

  // TODO: move to markup module
  // public void setAnnotator(UUID resourceUUID, String code, Annotator annotator) {
  // unwrap(delegate.setAnnotator(resourceUUID, code, annotator));
  // }
  // public Annotator getAnnotator(UUID resourceUUID, String code) {
  // return unwrap(delegate.getAnnotator(resourceUUID, code));
  // }
  //
  // public AnnotatorList getAnnotators(UUID resourceUUID) {
  // return unwrap(delegate.getAnnotators(resourceUUID));
  // }
  //
  // public void setResourceText(UUID resourceUUID, File file) throws IOException {
  // unwrap(delegate.setResourceText(resourceUUID, file));
  // }
  //
  // public void setResourceText(UUID resourceUUID, String xml) {
  // unwrap(delegate.setResourceText(resourceUUID, xml));
  // }
  //
  // public TextImportStatus getTextImportStatus(UUID resourceUUID) {
  // return unwrap(delegate.getTextImportStatus(resourceUUID));
  // }
  //
  // public TextEntity getTextInfo(UUID resourceUUID) {
  // return unwrap(delegate.getTextInfo(resourceUUID));
  // }
  //
  // public String getTextAsString(UUID uuid) {
  // return unwrap(delegate.getTextAsString(uuid));
  // }
  //
  // public String getTextAsString(UUID uuid, String viewName) {
  // return unwrap(delegate.getTextAsString(uuid, viewName));
  // }
  //
  // public String getTextAsString(UUID uuid, String viewName, Map<String, String> viewParameters) {
  // return unwrap(delegate.getTextAsString(uuid, viewName, viewParameters));
  // }
  //
  //
  // public String getTextAsDot(UUID uuid) {
  // return unwrap(delegate.getTextAsDot(uuid));
  // }
  //
  // public void addResourceTextRangeAnnotations(UUID resourceUUID, TextRangeAnnotationList textAnnotations) {
  // unwrap(delegate.addResourceTextRangeAnnotations(resourceUUID, textAnnotations));
  // }
  //
  // public TextRangeAnnotationInfo setResourceTextRangeAnnotation(UUID resourceUUID, TextRangeAnnotation textAnnotation) {
  // return unwrap(delegate.setResourceTextRangeAnnotation(resourceUUID, textAnnotation));
  // }
  //
  // public TextRangeAnnotation getResourceTextRangeAnnotation(UUID resourceUUID, UUID annotationUUID) {
  // return unwrap(delegate.getResourceTextRangeAnnotation(resourceUUID, annotationUUID));
  // }
  //
  // public void setResourceTextView(UUID resourceUUID, String textViewName, TextViewDefinition textView) {
  // unwrap(delegate.setResourceTextView(resourceUUID, textViewName, textView));
  // }
  //
  // public TextViewList getResourceTextViews(UUID uuid) {
  // return unwrap(delegate.getResourceTextViews(uuid));
  // }
  //
  // public TextViewDefinition getResourceTextView(UUID uuid, String textViewName) {
  // return unwrap(delegate.getResourceTextView(uuid, textViewName));
  // }
  //
  // public TextViewDefinition getResourceTextView(UUID uuid, String textViewName, Map<String, String> viewParameters) {
  // return unwrap(delegate.getResourceTextView(uuid, textViewName, viewParameters));
  // }
  // public TextRangeAnnotationList getResourceTextRangeAnnotations(UUID uuid) {
  // return unwrap(delegate.getResourceTextRangeAnnotations(uuid));
  // }
  //
  // public TextAnnotationImportStatus getResourceTextRangeAnnotationBatchImportStatus(UUID uuid) {
  // return unwrap(delegate.getResourceTextRangeAnnotationBatchImportStatus(uuid));
  // }
  // public CommandResponse doCommand(String commandName, Map<String, Object> parameters) {
  // return unwrap(delegate.doCommand(commandName, parameters));
  // }
  //
  // public CommandStatus getCommandStatus(String string, UUID uuid) {
  // return unwrap(delegate.getCommandStatus(string, uuid));
  // }

  /////// end delegated methods

  private <T> T unwrap(RestResult<T> restResult) {
    if (restResult.hasFailed()) {
      Optional<Response> response = restResult.getResponse();
      String status = response.map(response1 -> response1.getStatus() + ": ").orElse("");
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

  // TODO move to markup module
  // private TextImportStatus textImportStatusWhenFinished(UUID resourceUUID) {
  // TextImportStatus status = null;
  // boolean goOn = true;
  // while (goOn) {
  // try {
  // Thread.sleep(500);
  // } catch (InterruptedException e) {
  // e.printStackTrace();
  // }
  // status = unwrap(delegate.getTextImportStatus(resourceUUID));
  // goOn = !status.isDone();
  // }
  // return status;
  // }
  //
  // private TextAnnotationImportStatus textAnnotationImportStatusWhenFinished(UUID resourceUUID) {
  // TextAnnotationImportStatus status = null;
  // boolean goOn = true;
  // while (goOn) {
  // try {
  // Thread.sleep(500);
  // } catch (InterruptedException e) {
  // e.printStackTrace();
  // }
  // status = unwrap(delegate.getResourceTextRangeAnnotationBatchImportStatus(resourceUUID));
  // goOn = !status.isDone();
  // }
  // return status;
  // }

}
