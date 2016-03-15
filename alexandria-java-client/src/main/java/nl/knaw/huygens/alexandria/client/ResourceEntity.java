package nl.knaw.huygens.alexandria.client;

import java.net.URI;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

import nl.knaw.huygens.alexandria.api.model.JsonWrapperObject;
import nl.knaw.huygens.alexandria.api.model.PropertyPrefix;

@JsonTypeName("resource")
public class ResourceEntity extends JsonWrapperObject {
  String id;
  String ref;
  StateEntity state;

  @JsonProperty(PropertyPrefix.LINK + "annotations")
  List<URI> annotationURIs;

  @JsonProperty(PropertyPrefix.LINK + "provenance")
  URI provenanceURI;

  @JsonProperty(PropertyPrefix.LINK + "subresources")
  List<URI> subresourceURIs;

  @JsonProperty(PropertyPrefix.LINK + "text")
  URI textURI;

  @JsonProperty(PropertyPrefix.LINK + "baseLayerDefinition")
  URI baseLayerDefinitionURI;

  public URI getTextURI() {
    return textURI;
  }

  public void setTextURI(URI textURI) {
    this.textURI = textURI;
  }

  public URI getBaseLayerDefinitionURI() {
    return baseLayerDefinitionURI;
  }

  public void setBaseLayerDefinitionURI(URI baseLayerDefinitionURI) {
    this.baseLayerDefinitionURI = baseLayerDefinitionURI;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getRef() {
    return ref;
  }

  public void setRef(String ref) {
    this.ref = ref;
  }

  public List<URI> getAnnotationURIs() {
    return annotationURIs;
  }

  public void setAnnotationURIs(List<URI> annotationURIs) {
    this.annotationURIs = annotationURIs;
  }

  public URI getProvenanceURI() {
    return provenanceURI;
  }

  public void setProvenanceURI(URI provenanceURI) {
    this.provenanceURI = provenanceURI;
  }

  public List<URI> getSubresourceURIs() {
    return subresourceURIs;
  }

  public void setSubresourceURIs(List<URI> subresourceURIs) {
    this.subresourceURIs = subresourceURIs;
  }

  public StateEntity getState() {
    return state;
  }

  public void setState(StateEntity state) {
    this.state = state;
  }
}
