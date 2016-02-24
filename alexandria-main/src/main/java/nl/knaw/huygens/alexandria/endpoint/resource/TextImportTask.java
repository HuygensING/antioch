package nl.knaw.huygens.alexandria.endpoint.resource;

import static java.util.stream.Collectors.joining;

import java.io.InputStream;
import java.net.URI;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import org.apache.commons.io.IOUtils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.InstantSerializer;
import com.google.common.collect.Lists;

import nl.knaw.huygens.alexandria.endpoint.Entity;
import nl.knaw.huygens.alexandria.endpoint.JsonWrapperObject;
import nl.knaw.huygens.alexandria.endpoint.LocationBuilder;
import nl.knaw.huygens.alexandria.exception.BadRequestException;
import nl.knaw.huygens.alexandria.jaxrs.ThreadContext;
import nl.knaw.huygens.alexandria.model.AlexandriaAnnotation;
import nl.knaw.huygens.alexandria.model.AlexandriaAnnotationBody;
import nl.knaw.huygens.alexandria.model.AlexandriaResource;
import nl.knaw.huygens.alexandria.model.BaseLayerDefinition;
import nl.knaw.huygens.alexandria.model.TentativeAlexandriaProvenance;
import nl.knaw.huygens.alexandria.service.AlexandriaService;
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

  public TextImportTask(AlexandriaService service, LocationBuilder locationBuilder, BaseLayerDefinition bld, String xml, AlexandriaResource resource) {
    this.service = service;
    this.locationBuilder = locationBuilder;
    this.bld = bld;
    this.xml = xml;
    this.resource = resource;
    this.resourceId = resource.getId();
    this.status = new Status();
    status.setBaseLayerDefinitionURI(locationBuilder.locationOf(resource, ResourcesEndpoint.BASELAYERDEFINITION));
  }

  public Status getStatus() {
    return status;
  }

  @Override
  public void run() {
    BaseLayerData baseLayerData = TextUtil.extractBaseLayerData(xml, bld);
    if (baseLayerData.validationFailed()) {
      throw new BadRequestException(baseLayerData.getValidationErrors().stream().collect(joining("\n")));
    }
    service.setResourceTextFromStream(resourceId, streamIn(baseLayerData.getBaseLayer()));
    status.setBaseLayerURI(locationBuilder.locationOf(resource, "text"));

    baseLayerData.getAnnotationData().forEach(annotationData -> {
      AlexandriaTextLocator textLocator = new ByXPathTextLocator().withXPath(annotationData.getXPath());
      TentativeAlexandriaProvenance provenance = new TentativeAlexandriaProvenance(ThreadContext.getUserName(), Instant.now(), "initial text import");
      String type = annotationData.getType();
      String value = annotationData.getValue().toString();
      if (annotationData.getLevel().equals(XmlAnnotationLevel.element)) {
        value = type;
        type = TYPE_XML_ELEMENT;
      } else {
        type = TYPE_XML_ATTRIBUTE + type;
      }
      AlexandriaAnnotationBody annotationbody = service.createAnnotationBody(UUID.randomUUID(), type, value, provenance);
      AlexandriaAnnotation annotation = service.annotate(resource, textLocator, annotationbody, provenance);
      service.confirmAnnotation(annotation.getId());
      status.getGeneratedAnnotations().add(locationBuilder.locationOf(annotation));
      if (annotationData.getLevel().equals(XmlAnnotationLevel.element)) {
        Element element = (Element) annotationData.getValue();
        element.getAttributes().forEach((key, attributeValue) -> {
          AlexandriaAnnotationBody subannotationbody = service.createAnnotationBody(UUID.randomUUID(), TYPE_XML_ATTRIBUTE + key, attributeValue, provenance);
          AlexandriaAnnotation subAnnotation = service.annotate(annotation, subannotationbody, provenance);
          service.confirmAnnotation(subAnnotation.getId());
          status.getGeneratedAnnotations().add(locationBuilder.locationOf(subAnnotation));
        });
      }
    });
    status.setDone(true);
  }

  private InputStream streamIn(String body) {
    return IOUtils.toInputStream(body);
  }

  @JsonTypeName("textImportStatus")
  @JsonInclude(Include.NON_NULL)
  public static class Status extends JsonWrapperObject implements Entity {
    private boolean done = false;
    private List<URI> generatedAnnotations = Lists.newArrayList();
    private URI baseLayerDefinitionURI;
    private List<String> validationErrors = Lists.newArrayList();
    private Instant expires;
    private URI baseLayerURI;

    public URI getBaseLayerURI() {
      return baseLayerURI;
    }

    public boolean isDone() {
      return done;
    }

    public void setDone(boolean done) {
      this.done = done;
      this.expires = Instant.now().plus(1l, ChronoUnit.HOURS);
    }

    @JsonProperty(PropertyPrefix.LINK + "generatedAnnotations")
    public List<URI> getGeneratedAnnotations() {
      return generatedAnnotations;
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

  }

  public boolean isExpired() {
    return Instant.now().isAfter(status.getExpires());
  }

}
