package nl.knaw.huygens.alexandria.storage.frames;

/*
 * #%L
 * alexandria-service
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

import java.util.List;

import nl.knaw.huygens.alexandria.storage.VertexLabels;
import peapod.annotations.Edge;
import peapod.annotations.In;
import peapod.annotations.Out;
import peapod.annotations.Vertex;

@Vertex(VertexLabels.RESOURCE)
public abstract class ResourceVF extends AlexandriaVF {
  public static final String PART_OF = "part_of";

  public abstract String getCargo();

  public abstract void setCargo(String cargo);

  public abstract Boolean getHasText();

  public abstract void setHasText(Boolean hasText);

  public abstract void setSerializedTextViewMap(String json);

  public abstract String getSerializedTextViewMap();

  public abstract void setSerializedTextViewDefinitionMap(String json);

  public abstract String getSerializedTextViewDefinitionMap();

  @In
  @Edge(AnnotationVF.ANNOTATES_RESOURCE)
  public abstract List<AnnotationVF> getAnnotatedBy();

  @In
  @Edge(PART_OF)
  public abstract List<ResourceVF> getSubResources();

  @Out
  @Edge(PART_OF)
  public abstract ResourceVF getParentResource();

  @Out
  @Edge(PART_OF)
  public abstract void setParentResource(ResourceVF resourceVF);

  public boolean isSubresource() {
    return getParentResource() != null;
  }


}
