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
import nl.knaw.huygens.antioch.storage.frames.AnnotationVF.EdgeLabels;
import peapod.FramedVertex;
import peapod.annotations.Edge;
import peapod.annotations.In;
import peapod.annotations.Vertex;

@Vertex(VertexLabels.ANNOTATIONBODY)
public abstract class AnnotationBodyVF extends AntiochVF implements FramedVertex<AnnotationBodyVF> {
  public abstract void setType(String type);

  public abstract String getType();

  public abstract void setValue(String value);

  public abstract String getValue();

  @In
  @Edge(EdgeLabels.HAS_BODY)
  public abstract List<AnnotationVF> getOfAnnotationList();

}
