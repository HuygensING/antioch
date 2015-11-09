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

import nl.knaw.huygens.alexandria.storage.Labels;
import peapod.annotations.Edge;
import peapod.annotations.In;
import peapod.annotations.Vertex;

@Vertex(Labels.ANNOTATIONBODY)
public abstract class AnnotationBodyVF extends FullAlexandriaVF {
  public abstract void setType(String type);

  public abstract String getType();

  public abstract void setValue(String value);

  public abstract String getValue();

  @In
  @Edge(AnnotationVF.HAS_BODY)
  public abstract List<AnnotationVF> getOfAnnotationList();

}
