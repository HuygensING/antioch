package nl.knaw.huygens.alexandria.endpoint.resource;

import static java.util.stream.Collectors.joining;

import java.io.InputStream;
import java.net.URI;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import org.apache.commons.io.IOUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.InstantSerializer;
import com.google.common.collect.Lists;

import nl.knaw.huygens.alexandria.api.EndpointPaths;
import nl.knaw.huygens.alexandria.api.model.BaseLayerDefinition;
import nl.knaw.huygens.alexandria.api.model.Entity;
import nl.knaw.huygens.alexandria.api.model.JsonWrapperObject;
import nl.knaw.huygens.alexandria.api.model.PropertyPrefix;
import nl.knaw.huygens.alexandria.endpoint.LocationBuilder;
import nl.knaw.huygens.alexandria.exception.BadRequestException;
import nl.knaw.huygens.alexandria.model.AlexandriaAnnotation;
import nl.knaw.huygens.alexandria.model.AlexandriaAnnotationBody;
import nl.knaw.huygens.alexandria.model.AlexandriaResource;
import nl.knaw.huygens.alexandria.model.TentativeAlexandriaProvenance;
import nl.knaw.huygens.alexandria.service.AlexandriaService;
import nl.knaw.huygens.alexandria.text.AnnotationData;
import nl.knaw.huygens.alexandria.text.BaseLayerData;
import nl.knaw.huygens.alexandria.text.TextUtil;
import nl.knaw.huygens.alexandria.text.XmlAnnotationLevel;
import nl.knaw.huygens.alexandria.textlocator.AlexandriaTextLocator;
import nl.knaw.huygens.alexandria.textlocator.ByXPathTextLocator;
import nl.knaw.huygens.tei.Element;

public class TextImportTask implements Runnable {
  private static final String TYPE_XML_ELEMENT = "xml-element";
  private static final String TYPE_XML_ATTRIBUTE = "xml-attribute:";
  private AlexandriaService service;
  private LocationBuilder locationBuilder;
  private BaseLayerDefinition bld;
  private String xml;
  private UUID resourceId;
  private AlexandriaResource resource;
  private Status status;
  private String who;

  public TextImportTask(AlexandriaService service, LocationBuilder locationBuilder, BaseLayerDefinition bld, String xml, AlexandriaResource resource, String who) {
    this.service = service;
    this.locationBuilder = locationBuilder;
    this.bld = bld;
    this.xml = xml;
    this.resource = resource;
    this.who = who;
    this.resourceId = resource.getId();
    this.status = new Status();
    status.setBaseLayerDefinitionURI(locationBuilder.locationOf(resource, EndpointPaths.BASELAYERDEFINITION));
  }

  public Status getStatus() {
    return status;
  }

  @Override
  public void run() {
    status.setStarted();
    BaseLayerData baseLayerData = TextUtil.extractBaseLayerData(xml, bld);
    if (baseLayerData.validationFailed()) {
      throw new BadRequestException(baseLayerData.getValidationErrors().stream().collect(joining("\n")));
    }
    status.setBaseLayerURI(locationBuilder.locationOf(resource, "text"));
    status.setExpectedTotal(calculateExpectedTotal(baseLayerData));
    service.setResourceTextFromStream(resourceId, streamIn(baseLayerData.getBaseLayer()));

    generateAnnotationsAndSubresources(resource, baseLayerData);

    status.setDone();
  }

  private static int calculateExpectedTotal(BaseLayerData baseLayerData) {
    return baseLayerData.getAnnotationData().size() +
      baseLayerData.getSubLayerData().stream().map(TextImportTask::calculateExpectedTotal).mapToInt(i -> i).sum();
  }

  private void generateAnnotationsAndSubresources(AlexandriaResource parentResource, BaseLayerData baseLayerData) {
    generateAnnotations(parentResource, baseLayerData.getAnnotationData());
    generateSubresources(parentResource, baseLayerData.getSubLayerData());
  }

  private void generateAnnotations(AlexandriaResource parentResource, List<AnnotationData> annotationDataList) {
    annotationDataList.forEach(annotationData -> {
      AlexandriaTextLocator textLocator = new ByXPathTextLocator().withXPath(annotationData.getXPath());
      TentativeAlexandriaProvenance provenance = newProvenance();
      String type = annotationData.getType();
      String value = annotationData.getValue().toString();
      boolean annotationIsXmlElementAnnotation = annotationData.getLevel().equals(XmlAnnotationLevel.element);
      if (annotationIsXmlElementAnnotation) {
        value = type;
        type = TYPE_XML_ELEMENT;
      } else {
        type = TYPE_XML_ATTRIBUTE + type;
      }
      AlexandriaAnnotationBody annotationbody = service.createAnnotationBody(UUID.randomUUID(), type, value, provenance);
      AlexandriaAnnotation annotation = service.annotate(parentResource, textLocator, annotationbody, provenance);
      service.confirmAnnotation(annotation.getId());
      URI annotationURI = locationBuilder.locationOf(annotation);
      if (annotationIsXmlElementAnnotation) {
        status.getGeneratedXmlElementAnnotations().add(annotationURI);
        Element element = (Element) annotationData.getValue();
        element.getAttributes().forEach((key, attributeValue) -> {
          AlexandriaAnnotationBody subannotationbody = service.createAnnotationBody(UUID.randomUUID(), TYPE_XML_ATTRIBUTE + key, attributeValue, provenance);
          AlexandriaAnnotation subAnnotation = service.annotate(annotation, subannotationbody, provenance);
          service.confirmAnnotation(subAnnotation.getId());
          status.getGeneratedXmlElementAttributeAnnotations().add(locationBuilder.locationOf(subAnnotation));
        });
      }
    });
  }

  private void generateSubresources(AlexandriaResource parentResource, List<BaseLayerData> sublayerData) {
    sublayerData.forEach(data -> {
      TentativeAlexandriaProvenance provenance = newProvenance();
      UUID subresourceId = UUID.randomUUID();
      AlexandriaResource subresource = service.createSubResource(subresourceId, parentResource.getId(), data.getId(), provenance);
      service.setResourceTextFromStream(subresourceId, streamIn(data.getBaseLayer()));
      service.confirmResource(subresourceId);
      status.getGeneratedSubresources().add(locationBuilder.locationOf(subresource));
      generateAnnotationsAndSubresources(subresource, data);
    });
  }

  private TentativeAlexandriaProvenance newProvenance() {
    TentativeAlexandriaProvenance provenance = new TentativeAlexandriaProvenance(who, Instant.now(), "initial text import");
    return provenance;
  }

  private InputStream streamIn(String body) {
    return IOUtils.toInputStream(body);
  }

  @JsonTypeName("textImportStatus")
  @JsonInclude(Include.NON_NULL)
  public static class Status extends JsonWrapperObject implements Entity {
    enum State {waiting, processing, done}

    private boolean done = false;
    private State state = State.waiting;
    private List<URI> generatedXmlElementAnnotations = Lists.newArrayList();
    private List<URI> generatedXmlElementAttributeAnnotations = Lists.newArrayList();
    private List<URI> generatedSubresources = Lists.newArrayList();
    private List<String> validationErrors = Lists.newArrayList();
    private URI baseLayerDefinitionURI;
    private URI baseLayerURI;
    private Instant expires;
    private float expectedTotal = 0;

    public void setExpectedTotal(float expectedTotal) {
      this.expectedTotal = expectedTotal;
    }

    public URI getBaseLayerURI() {
      return baseLayerURI;
    }

    public boolean isDone() {
      return done;
    }

    public State getState() {
      return state;
    }

    public void setStarted() {
      this.state = State.processing;
    }

    @JsonIgnore
    public boolean isExpired() {
      return expires != null && Instant.now().isAfter(expires);
    }

    public void setDone() {
      this.done = true;
      this.state = State.done;
      this.expires = Instant.now().plus(1l, ChronoUnit.HOURS);
    }

    public Integer getXmlElementAnnotationsGenerated() {
      return generatedXmlElementAnnotations.size();
    }

    @JsonProperty(PropertyPrefix.LINK + "generatedXmlElementAnnotations")
    public List<URI> getGeneratedXmlElementAnnotations() {
      return generatedXmlElementAnnotations;
    }

    public Integer getXmlElementAttributeAnnotationsGenerated() {
      return generatedXmlElementAttributeAnnotations.size();
    }

    @JsonProperty(PropertyPrefix.LINK + "generatedXmlElementAttributeAnnotations")
    public List<URI> getGeneratedXmlElementAttributeAnnotations() {
      return generatedXmlElementAttributeAnnotations;
    }

    public Integer getSubresourcesGenerated() {
      return generatedSubresources.size();
    }

    @JsonProperty(PropertyPrefix.LINK + "generatedSubresources")
    public List<URI> getGeneratedSubresources() {
      return generatedSubresources;
    }

    @JsonProperty(PropertyPrefix.LINK + "baseLayerDefinition")
    public URI getBaseLayerDefinitionURI() {
      return baseLayerDefinitionURI;
    }

    public void setBaseLayerDefinitionURI(URI baseLayerDefinitionURI) {
      this.baseLayerDefinitionURI = baseLayerDefinitionURI;
    }

    @JsonProperty(PropertyPrefix.LINK + "baseLayer")
    public void setBaseLayerURI(URI baseLayerURI) {
      this.baseLayerURI = baseLayerURI;
    }

    public List<String> getValidationErrors() {
      return validationErrors;
    }

    @JsonSerialize(using = InstantSerializer.class)
    public Instant getExpires() {
      return expires;
    }

    public float getPercentageDone() {
      return expectedTotal == 0 ? 0 : (getXmlElementAnnotationsGenerated() * 100) / expectedTotal;
    }

  }

}
