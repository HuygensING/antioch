package nl.knaw.huygens.alexandria.textgraph;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.junit.Test;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import nl.knaw.huygens.Log;
import nl.knaw.huygens.alexandria.util.StreamUtil;
import nl.knaw.huygens.tei.QueryableDocument;

public class XPathAnnotationTest {
  @Test
  public void testXpathAnnotation1() throws XPathExpressionException, ParserConfigurationException, SAXException, IOException {
    String xpathQuery = "//p"; //
    String xml = "<text><p xml:id=\"a2\">first para.</p><p xml:id=\"a3\">second para.</p></text>";

    QueryableDocument qd = QueryableDocument.createFromXml(xml, true);
    NodeList nodelist = qd.evaluateXPathToW3CNodeList(xpathQuery);
    StreamUtil.stream(nodelist).forEach(node -> {
      short nodeType = node.getNodeType();
      switch (nodeType) {
      case Node.ELEMENT_NODE:
        Log.info("node.firstChild = {}, node.xml:id = {}", //
            node.getFirstChild().getNodeValue(), //
            node.getAttributes().getNamedItem("xml:id"));
        break;

      case Node.TEXT_NODE:
        Log.info("node.nodevalue = {}, node.parent.xml:id = {}", //
            node.getNodeValue(), //
            node.getParentNode().getAttributes().getNamedItem("xml:id"));
        break;

      default:
        Log.info("nodes of type {} are not supported.", nodeType);
        break;
      }
    });
  }

  // to be able to map the nodes corresponding to the xpath to the textannotations in the database, the textannotations should each have a unique id, which is returned as an attribute in the xml
  // generated for the querying with xpath.

}
