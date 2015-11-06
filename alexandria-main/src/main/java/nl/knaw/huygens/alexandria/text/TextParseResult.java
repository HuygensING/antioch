package nl.knaw.huygens.alexandria.text;

import java.util.List;
<<<<<<< 39c96eabda13be48f8df7cfd776165e816f9f875
import java.util.Map;

import com.google.common.collect.Lists;

import jersey.repackaged.com.google.common.collect.Maps;

public class TextParseResult {
  private List<TextNode> textNodes = Lists.newArrayList();
  private List<TextRange> textRanges = Lists.newArrayList();
  private Map<Tag, TextRange> tag2textRangeMap = Maps.newHashMap();
  private String parseError = null;
=======

import com.google.common.collect.Lists;

public class TextParseResult {

  private List<TextNode> textNodes = Lists.newArrayList();
  private List<TextRange> textRanges = Lists.newArrayList();
>>>>>>> [NLA-132] parse xml to determine textnodes, textranges

  public List<TextNode> getTextNodes() {
    return textNodes;
  }

  public List<TextRange> getTextRanges() {
    return textRanges;
  }

<<<<<<< 39c96eabda13be48f8df7cfd776165e816f9f875
  public Map<Tag, TextRange> getTag2TextRangeMap() {
    return tag2textRangeMap;
  }

  public TextNode getLastTextNode() {
    return textNodes.get(textNodes.size() - 1);
  }

  public TextParseResult setParseError(String message) {
    this.parseError = message;
    return this;
  }

  public String getParseError() {
    return parseError;
  }

  public boolean isOK() {
    return parseError == null;
  }
=======
>>>>>>> [NLA-132] parse xml to determine textnodes, textranges
}
