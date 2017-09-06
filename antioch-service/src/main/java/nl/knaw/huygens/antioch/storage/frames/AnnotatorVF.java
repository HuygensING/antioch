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


import nl.knaw.huygens.antioch.storage.VertexLabels;
import peapod.FramedVertex;
import peapod.annotations.Edge;
import peapod.annotations.Out;
import peapod.annotations.Vertex;

@Vertex(VertexLabels.ANNOTATOR)
public abstract class AnnotatorVF implements VF, FramedVertex<AnnotatorVF> {
  public static class EdgeLabels {
    public static final String HAS_RESOURCE = "annotator_has_resource";
  }

  public abstract void setCode(String code);

  public abstract String getCode();

  public abstract void setDescription(String description);

  public abstract String getDescription();

  @Out
  @Edge(EdgeLabels.HAS_RESOURCE)
  public abstract void setResource(ResourceVF resource);

  @Out
  @Edge(EdgeLabels.HAS_RESOURCE)
  public abstract ResourceVF getResource();

}
