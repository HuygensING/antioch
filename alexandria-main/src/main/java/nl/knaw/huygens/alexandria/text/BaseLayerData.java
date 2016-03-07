package nl.knaw.huygens.alexandria.text;

import java.util.ArrayList;
import java.util.List;

public class BaseLayerData {
  private String id = "";
  private String baseLayer = "";
  private List<String> annotationActions;
  private List<AnnotationData> annotationData;
  private List<String> validationErrors = new ArrayList<>();
  private List<BaseLayerData> subLayerData = new ArrayList<>();

  public static BaseLayerData withBaseLayer(String baseLayer) {
    BaseLayerData bld = new BaseLayerData();
    bld.baseLayer = baseLayer;
    return bld;
  }

  public BaseLayerData withAnnotationDryRun(List<String> annotationActions) {
    this.annotationActions = annotationActions;
    return this;
  }

  public BaseLayerData withAnnotationData(List<AnnotationData> annotationData) {
    this.annotationData = annotationData;
    return this;
  }

  public BaseLayerData withValidationErrors(List<String> validationerrors) {
    this.validationErrors = validationerrors;
    return this;
  }

  public BaseLayerData withId(String id) {
    this.id = id;
    return this;
  }

  public String getBaseLayer() {
    return baseLayer;
  }

  public List<String> getAnnotationActions() {
    return annotationActions;
  }

  public List<AnnotationData> getAnnotationData() {
    return annotationData;
  }

  public List<BaseLayerData> getSubLayerData() {
    return subLayerData;
  }

  public List<String> getValidationErrors() {
    return validationErrors;
  }

  public Boolean validationFailed() {
    return !validationErrors.isEmpty();
  }

  public String getId() {
    return id;
  }

}
