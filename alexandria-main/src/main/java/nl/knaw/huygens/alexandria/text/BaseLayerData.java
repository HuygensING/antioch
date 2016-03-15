package nl.knaw.huygens.alexandria.text;

/*
 * #%L
 * alexandria-main
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
