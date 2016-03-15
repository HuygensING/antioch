package nl.knaw.huygens.alexandria.text;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import nl.knaw.huygens.tei.XmlContext;

public class SEVContext extends XmlContext {
  private Map<String, String> subresourceTexts = new HashMap<>();
  private boolean inSubresourceText = false;
  private AtomicInteger subtextCounter = new AtomicInteger(1);
  private String rootElementName = "xml";

  public AtomicInteger getSubtextCounter() {
    return subtextCounter;
  }

  public void setSubtextCounter(AtomicInteger subtextCounter) {
    this.subtextCounter = subtextCounter;
  }

  public boolean isInSubresourceText() {
    return inSubresourceText;
  }

  public String getRootElementName() {
    return rootElementName;
  }

  public void setSubresourceTexts(Map<String, String> subresourceTexts) {
    this.subresourceTexts = subresourceTexts;
  }

  public Map<String, String> getSubresourceTexts() {
    return subresourceTexts;
  }

  public void setRootElementName(String name) {
    this.rootElementName = name;
  }

  public boolean inSubresourceText() {
    return inSubresourceText;
  }

  public void setInSubresourceText(boolean b) {
    this.inSubresourceText = b;
  }

  public String getBaseText() {
    return getResult();
  }

}
