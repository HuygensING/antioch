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

import java.util.Collection;
import java.util.Set;

import com.google.common.collect.Multimap;
import com.google.common.collect.Multiset;
import com.google.common.collect.TreeMultimap;
import com.google.common.collect.TreeMultiset;

import nl.knaw.huygens.Log;
import nl.knaw.huygens.tei.Element;

public class ElementTally {

  Multiset<String> elementNameSet = TreeMultiset.create();
  Multimap<String, String> elementAttributeSet = TreeMultimap.create();

  public void tally(Element element) {
    String name = element.getName();
    elementNameSet.add(name);
    element.getAttributeNames()//
        .forEach(a -> elementAttributeSet.put(name, a));
  }

  public Set<String> getElementNames() {
    return elementNameSet.elementSet();
  }

  public Integer getElementCount(String elementName) {
    return elementNameSet.count(elementName);
  }

  public Collection<String> getAttributeNames(String elementName) {
    return elementAttributeSet.get(elementName);
  }

  public Long getElementAttributeCount(String elementName, String attributeName) {
    return elementAttributeSet.get(elementName).stream()//
        .filter(a -> a.equals(attributeName))//
        .count();
  }

  public void logReport() {
    getElementNames().forEach(e -> {
      Log.info("<{}> : {}", e, getElementCount(e));
      getAttributeNames(e).forEach(a -> Log.info("  {} : {}", a, getElementAttributeCount(e, a)));
    });
  }

}
