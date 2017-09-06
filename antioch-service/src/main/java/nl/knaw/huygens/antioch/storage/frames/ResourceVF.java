package nl.knaw.huygens.antioch.storage.frames;

/*
 * #%L
 * antioch-service
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

import java.util.List;

import nl.knaw.huygens.antioch.storage.VertexLabels;
import peapod.FramedVertex;
import peapod.annotations.Edge;
import peapod.annotations.In;
import peapod.annotations.Out;
import peapod.annotations.Vertex;

@Vertex(VertexLabels.RESOURCE)
public abstract class ResourceVF extends AntiochVF implements FramedVertex<ResourceVF> {
  public static class Properties {
    public static final String CARGO = "cargo";
  }

  public static class EdgeLabels {
    public static final String PART_OF = "part_of";
  }

  public abstract String getCargo();

  public abstract void setCargo(String cargo);

  public abstract Boolean getHasText();

  public abstract void setHasText(Boolean hasText);

  public abstract void setSerializedTextViewMap(String json);

  public abstract String getSerializedTextViewMap();

  public abstract void setSerializedTextViewDefinitionMap(String json);

  public abstract String getSerializedTextViewDefinitionMap();

  @In
  @Edge(nl.knaw.huygens.antioch.storage.frames.AnnotationVF.EdgeLabels.ANNOTATES_RESOURCE)
  public abstract List<AnnotationVF> getAnnotatedBy();

  @In
  @Edge(EdgeLabels.PART_OF)
  public abstract List<ResourceVF> getSubResources();

  @In
  @Edge(nl.knaw.huygens.antioch.storage.frames.AnnotatorVF.EdgeLabels.HAS_RESOURCE)
  public abstract List<AnnotatorVF> getAnnotators();

  // @In
  // @Edge(nl.knaw.huygens.antioch.storage.frames.TextRangeAnnotationVF.EdgeLabels.HAS_RESOURCE)
  // public abstract List<TextRangeAnnotationVF> getTextRangeAnnotations();

  @Out
  @Edge(EdgeLabels.PART_OF)
  public abstract ResourceVF getParentResource();

  @Out
  @Edge(EdgeLabels.PART_OF)
  public abstract void setParentResource(ResourceVF resourceVF);

  public boolean isSubResource() {
    return getParentResource() != null;
  }

}
