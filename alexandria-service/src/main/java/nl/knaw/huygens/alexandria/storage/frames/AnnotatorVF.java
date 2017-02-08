package nl.knaw.huygens.alexandria.storage.frames;

/*
 * #%L
 * alexandria-service
 * =======
 * Copyright (C) 2015 - 2017 Huygens ING (KNAW)
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


import nl.knaw.huygens.alexandria.storage.VertexLabels;
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
