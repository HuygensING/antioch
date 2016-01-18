package nl.knaw.huygens.alexandria.text;

import java.io.StringReader;
import java.util.Optional;
import java.util.UUID;

import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import nl.knaw.huygens.alexandria.endpoint.resource.text.TextQuery;
import nl.knaw.huygens.alexandria.endpoint.resource.text.TextQueryResult;

public interface TextService {
  // Create XPathFactory object
  XPathFactory xpathFactory = XPathFactory.newInstance();
  // Create XPath object
  XPath xpath = xpathFactory.newXPath();

  void set(UUID resourceUUID, String text);

  Optional<String> get(UUID resourceUUID);

  public default TextQueryResult executeQuery(TextQuery textQuery) {
    TextQueryResult result = new TextQueryResult();
    String type = textQuery.getType();
    if ("xpath".equals(type)) {
      String text = get(textQuery.getResourceUUID()).orElse("");
      try {
        InputSource source = new InputSource(new StringReader(text));
        NodeList nodes = (NodeList) xpath.evaluate(textQuery.getQuery(), source, XPathConstants.NODESET);
        for (int i = 0; i < nodes.getLength(); i++) {
          Node node = nodes.item(i);
          result.addResult(XmlUtil.nodeToString(node));
        }

      } catch (XPathExpressionException | TransformerFactoryConfigurationError | TransformerException e) {
        e.printStackTrace();
        result.addError(e.getMessage());
      }

    } else {
      result.addError("type not recognized:  " + type);
    }
    return result;
  };

}
