package nl.knaw.huygens.alexandria.text;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;

import jersey.repackaged.com.google.common.collect.Maps;

public class TextParseResult {

  private List<TextNode> textNodes = Lists.newArrayList();
  private List<TextRange> textRanges = Lists.newArrayList();
  private Map<Tag, TextRange> tag2textRangeMap = Maps.newHashMap();

  public List<TextNode> getTextNodes() {
    return textNodes;
  }

  public List<TextRange> getTextRanges() {
    return textRanges;
  }

  public Map<Tag, TextRange> getTag2TextRangeMap() {
    return tag2textRangeMap;
  }

  public TextNode getLastTextNode() {
    return textNodes.get(textNodes.size() - 1);
  }

}
