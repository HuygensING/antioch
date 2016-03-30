package nl.knaw.huygens.alexandria.textgraph;

import static java.util.stream.Collectors.joining;

import java.io.InputStream;
import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.apache.commons.io.IOUtils;

import nl.knaw.huygens.alexandria.api.EndpointPaths;
import nl.knaw.huygens.alexandria.api.model.BaseLayerDefinition;
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

public class TextGraphImportTask implements Runnable {

  private static final String TYPE_XML_ELEMENT = "xml-element";
  private static final String TYPE_XML_ATTRIBUTE = "xml-attribute:";
  private AlexandriaService service;
  private LocationBuilder locationBuilder;
  private BaseLayerDefinition bld;
  private String xml;
  private UUID resourceId;
  private AlexandriaResource resource;
  private TextGraphImportStatus status;
  private String who;

  public TextGraphImportTask(AlexandriaService service, LocationBuilder locationBuilder, BaseLayerDefinition bld, String xml, AlexandriaResource resource, String who) {
    this.service = service;
    this.locationBuilder = locationBuilder;
    this.bld = bld;
    this.xml = xml;
    this.resource = resource;
    this.who = who;
    this.resourceId = resource.getId();
    this.status = new TextGraphImportStatus();
    status.setBaseLayerDefinitionURI(locationBuilder.locationOf(resource, EndpointPaths.BASELAYERDEFINITION));
  }

  public TextGraphImportStatus getStatus() {
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
    return baseLayerData.getAnnotationData().size() + baseLayerData.getSubLayerData().stream().map(TextGraphImportTask::calculateExpectedTotal).mapToInt(i -> i).sum();
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

}
