package nl.knaw.huygens.alexandria.text;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class GremlinGenerator {
  public static String from(TextParseResult tpr) {
    StringBuilder gremlinBuilder = new StringBuilder();
    gremlinBuilder.append("graph = TinkerGraph.open()\n");
    gremlinBuilder.append("g = graph.traversal(standard())\n");
    List<TextNode> textNodes = tpr.getTextNodes();
    int i = 0;
    Map<TextNode, String> textNodeIndex = new HashMap<>();
    for (TextNode textNode : textNodes) {
      String name = "textNode" + i;
      gremlinBuilder.append(name + " = graph.addVertex(label, 'TextNode', 'text', '" + textNode.getText().replace("\n", "<br>") + "')\n");
      textNodeIndex.put(textNode, name);
      if (i > 0) {
        gremlinBuilder.append("textNode" + (i - 1) + ".addEdge('next_node', textNode" + i + ")\n");
      }
      i++;
    }

    List<TextRange> textRanges = tpr.getTextRanges();
    Map<TextRange, String> textRangeIndex = new HashMap<>();
    i = 0;
    for (TextRange textRange : textRanges) {
      String variable = "textrange" + i++;
      textRangeIndex.put(textRange, variable);
      gremlinBuilder.append(variable + " = graph.addVertex(label, 'TextRange')\n");
      String firstNode = textNodeIndex.get(textRange.getFirstNode());
      String lastNode = textNodeIndex.get(textRange.getLastNode());
      gremlinBuilder.append(variable + ".addEdge('first_textnode', " + firstNode + ")\n");
      gremlinBuilder.append(variable + ".addEdge('last_textnode', " + lastNode + ")\n");
      gremlinBuilder.append("print 'range[" + firstNode + "," + lastNode + "] : '\n");
      gremlinBuilder.append("startNode = g.V(" + variable + ").out('first_textnode').next()\n");
      gremlinBuilder.append("endNode = g.V(" + variable + ").out('last_textnode').next()\n");
      gremlinBuilder.append("g.V(startNode).repeat(out('next_node')).until(is(endNode)).path().by('text')\n");
    }

    final AtomicInteger ai = new AtomicInteger();
    tpr.getTag2TextRangeMap().forEach((tag, textrange) -> {
      String variable = "tag" + ai.getAndIncrement();
      gremlinBuilder.append(variable + " = graph.addVertex(label, 'Tag', 'name', '" + tag.getName() + "')\n");
      gremlinBuilder.append(variable + ".addEdge('annotates', " + textRangeIndex.get(textrange) + ")\n");
    });


    return gremlinBuilder.toString();
  }
}
