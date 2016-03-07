package nl.knaw.huygens.alexandria.stanfordnlp;

/*
 * #%L
 * alexandria-performance-tests
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

import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.util.Properties;

import static nl.knaw.huygens.alexandria.gutenberg.Text.TEI_NS_URI;

/**
 * @author <a href="http://gregor.middell.net/">Gregor Middell</a>
 */
public class Pipeline {

  private final StanfordCoreNLP nlp;

  public Pipeline() {
    final Properties props = new Properties();
    props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner");
    nlp = new StanfordCoreNLP(props);
  }


  public Annotation annotate(String text) {
    final Annotation annotation = new Annotation(text);
    nlp.annotate(annotation);
    return annotation;
  }

  public void writeAnnotated(XMLStreamWriter xml, String text) throws XMLStreamException {
    int offset = 0;
    xml.writeStartElement(TEI_NS_URI, "ab");
    for (CoreMap sentence: annotate(text).get(SentencesAnnotation.class)) {
      boolean sentenceStarted = false;
      for (CoreLabel token : sentence.get(TokensAnnotation.class)) {
        final int tokenStart = token.beginPosition();
        writeText(xml, text.substring(offset, tokenStart));
        if (!sentenceStarted) {
          xml.writeStartElement(TEI_NS_URI, "s");
          sentenceStarted = true;
        }
        xml.writeStartElement(TEI_NS_URI, "w");
        xml.writeAttribute("lemma", token.get(LemmaAnnotation.class));
        xml.writeAttribute("type", token.get(PartOfSpeechAnnotation.class));
        xml.writeAttribute("function", token.get(NamedEntityTagAnnotation.class));
        writeText(xml, text.substring(tokenStart, offset = token.endPosition()));
        xml.writeEndElement();
      }
      if (sentenceStarted) {
        xml.writeEndElement();
      }
    }
    xml.writeEndElement();
  }

  private void writeText(XMLStreamWriter xml, String text) throws XMLStreamException {
    final int length = text.length();
    int offset = 0;
    int lb;
    while (offset < length) {
      lb = text.indexOf('\n', offset);
      if (offset < lb) {
        xml.writeCharacters(text.substring(offset, lb));
      }
      if (lb >= 0) {
        xml.writeEmptyElement(TEI_NS_URI, "lb");
        offset = lb + 1;
      } else {
        xml.writeCharacters(text.substring(offset, length));
        break;
      }
    }
  }
}
