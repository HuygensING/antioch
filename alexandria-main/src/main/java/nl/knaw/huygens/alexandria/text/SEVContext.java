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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import nl.knaw.huygens.tei.XmlContext;

public class SEVContext extends XmlContext {
  private Map<String, String> subresourceTexts = new HashMap<>();
  private boolean inSubresourceText = false;
  private AtomicInteger subtextCounter = new AtomicInteger(1);

  public AtomicInteger getSubtextCounter() {
    return subtextCounter;
  }

  public void setSubtextCounter(AtomicInteger subtextCounter) {
    this.subtextCounter = subtextCounter;
  }

  public boolean isInSubresourceText() {
    return inSubresourceText;
  }

  public void setSubresourceTexts(Map<String, String> subresourceTexts) {
    this.subresourceTexts = subresourceTexts;
  }

  public Map<String, String> getSubresourceTexts() {
    return subresourceTexts;
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
