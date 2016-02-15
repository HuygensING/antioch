package nl.knaw.huygens.alexandria.text;

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
      getAttributeNames(e).forEach(a -> {
        Log.info("  {} : {}", a, getElementAttributeCount(e, a));
      });
    });
  }

}
