package nl.knaw.huygens.alexandria.text;

import java.util.List;

import com.google.common.collect.Lists;

public class TextParseResult {

  private List<TextNode> textNodes = Lists.newArrayList();
  private List<TextRange> textRanges = Lists.newArrayList();

  public List<TextNode> getTextNodes() {
    return textNodes;
  }

  public List<TextRange> getTextRanges() {
    return textRanges;
  }

}
