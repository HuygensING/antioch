package nl.knaw.huygens.alexandria.text;

import java.util.List;

public class BaseLayerData {
  private String baseLayer = "";
  private List<String> annotationActions;

  public static BaseLayerData withBaseLayer(String baseLayer) {
    BaseLayerData bld = new BaseLayerData();
    bld.baseLayer = baseLayer;
    return bld;
  }

  public BaseLayerData withAnnotationDryRun(List<String> annotationActions) {
    this.annotationActions = annotationActions;
    return this;
  }

  public String getBaseLayer() {
    return baseLayer;
  }

  public List<String> getAnnotationActions() {
    return annotationActions;
  }
}
