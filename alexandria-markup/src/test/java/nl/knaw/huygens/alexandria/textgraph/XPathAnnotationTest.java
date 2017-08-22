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
