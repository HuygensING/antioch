package nl.knaw.huygens.alexandria.textgraph;

/*
 * #%L
 * alexandria-main
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

import java.util.Map;

import org.apache.commons.lang3.builder.CompareToBuilder;

public class XmlAnnotation extends TextAnnotation implements Comparable<XmlAnnotation> {
  private boolean isMilestone;
  private Integer firstSegmentIndex;
  private Integer lastSegmentIndex;

  public XmlAnnotation(String name, Map<String, String> attributes, int depth) {
    super(name, attributes, depth);
  }

  public XmlAnnotation setMilestone(boolean bool) {
    this.isMilestone = bool;
    return this;
  }

  public boolean isMilestone() {
    return isMilestone;
  }

  public XmlAnnotation setFirstSegmentIndex(Integer firstSegmentIndex) {
    this.firstSegmentIndex = firstSegmentIndex;
    return this;
  }

  public Integer getFirstSegmentIndex() {
    return firstSegmentIndex;
  }

  public XmlAnnotation setLastSegmentIndex(Integer lastSegmentIndex) {
    this.lastSegmentIndex = lastSegmentIndex;
    return this;
  }

  public Integer getLastSegmentIndex() {
    return lastSegmentIndex;
  }

  @Override
  public int compareTo(XmlAnnotation other) {
    return new CompareToBuilder()//
        .append(this.firstSegmentIndex, other.firstSegmentIndex)//
        .append(other.lastSegmentIndex, this.lastSegmentIndex)//
        .append(getDepth(), other.getDepth())//
        .build();
  }

  @Override
  public String toString() {
    return "XmlAnnotation[" + getDepth() + ":" + firstSegmentIndex + "-" + lastSegmentIndex + "] <" + getName() + " " + getAttributes() + (isMilestone ? "/>" : ">");
  }

}
