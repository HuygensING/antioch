package nl.knaw.huygens.alexandria.client;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.UUID;

import javax.net.ssl.SSLContext;

import nl.knaw.huygens.alexandria.api.model.AboutEntity;
import nl.knaw.huygens.alexandria.api.model.Annotator;
import nl.knaw.huygens.alexandria.api.model.AnnotatorList;
import nl.knaw.huygens.alexandria.api.model.CommandResponse;
import nl.knaw.huygens.alexandria.api.model.search.AlexandriaQuery;
import nl.knaw.huygens.alexandria.api.model.search.SearchResultPage;
import nl.knaw.huygens.alexandria.api.model.text.TextEntity;
import nl.knaw.huygens.alexandria.api.model.text.TextImportStatus;
import nl.knaw.huygens.alexandria.api.model.text.TextRangeAnnotation;
import nl.knaw.huygens.alexandria.api.model.text.TextRangeAnnotationInfo;
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

  public TextImportStatus setResourceTextSynchronously(UUID resourceUUID, File file) throws IOException {
    unwrap(delegate.setResourceText(resourceUUID, file));
    return textImportStatusWhenFinished(resourceUUID);
  }

  public TextImportStatus setResourceTextSynchronously(UUID resourceUUID, String xml) {
    unwrap(delegate.setResourceText(resourceUUID, xml));
    return textImportStatusWhenFinished(resourceUUID);
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

  public void setAnnotator(UUID resourceUUID, String code, Annotator annotator) {
    unwrap(delegate.setAnnotator(resourceUUID, code, annotator));
  }

  public Annotator getAnnotator(UUID resourceUUID, String code) {
    return unwrap(delegate.getAnnotator(resourceUUID, code));
  }

  public AnnotatorList getAnnotators(UUID resourceUUID) {
    return unwrap(delegate.getAnnotators(resourceUUID));
  }

  public URI setResourceText(UUID resourceUUID, File file) throws IOException {
    return unwrap(delegate.setResourceText(resourceUUID, file));
  }

  public URI setResourceText(UUID resourceUUID, String xml) {
    return unwrap(delegate.setResourceText(resourceUUID, xml));
  }

  public TextImportStatus getTextImportStatus(UUID resourceUUID) {
    return unwrap(delegate.getTextImportStatus(resourceUUID));
  }

  public TextEntity getTextInfo(UUID resourceUUID) {
    return unwrap(delegate.getTextInfo(resourceUUID));
  }

  public String getTextAsString(UUID uuid) {
    return unwrap(delegate.getTextAsString(uuid));
  }

  public String getTextAsString(UUID uuid, String viewName) {
    return unwrap(delegate.getTextAsString(uuid, viewName));
  }

  public String getTextAsDot(UUID uuid) {
    return unwrap(delegate.getTextAsDot(uuid));
  }

  public TextRangeAnnotationInfo setResourceTextRangeAnnotation(UUID resourceUUID, TextRangeAnnotation textAnnotation) {
    return unwrap(delegate.setResourceTextRangeAnnotation(resourceUUID, textAnnotation));
  }

  public TextRangeAnnotation getResourceTextRangeAnnotation(UUID resourceUUID, UUID annotationUUID) {
    return unwrap(delegate.getResourceTextRangeAnnotation(resourceUUID, annotationUUID));
  }

  public URI setResourceTextView(UUID resourceUUID, String textViewName, TextViewDefinition textView) {
    return unwrap(delegate.setResourceTextView(resourceUUID, textViewName, textView));
  }

  public TextView getResourceTextView(UUID uuid) {
    return unwrap(delegate.getResourceTextView(uuid));
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

  public CommandResponse addCommand(String commandName, Map<String, Object> parameters) {
    return unwrap(delegate.addCommand(commandName, parameters));
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
  /////// end delegated methods

  private <T> T unwrap(RestResult<T> restResult) {
    if (restResult.hasFailed()) {
      throw new AlexandriaException(restResult.getFailureCause().orElse("Unspecified error"));
    }
    return restResult.get();
  }

  private ResourcePrototype resourceWithRef(String ref) {
    return new ResourcePrototype().setRef(ref);
  }

  private SubResourcePrototype subResourceWithSub(String sub) {
    return new SubResourcePrototype().setSub(sub);
  }

  private TextImportStatus textImportStatusWhenFinished(UUID resourceUUID) {
    TextImportStatus status = null;
    boolean goOn = true;
    while (goOn) {
      try {
        Thread.sleep(500);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      status = unwrap(delegate.getTextImportStatus(resourceUUID));
      goOn = !status.isDone();
    }
    return status;
  }
}
