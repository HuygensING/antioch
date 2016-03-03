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
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import nl.knaw.huygens.Log;
import nl.knaw.huygens.alexandria.gutenberg.Text;

import java.io.IOException;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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


  public void annotate(Text text) throws IOException {
    annotate(Text.body(text.contents()));
  }

  public void annotate(String text) {
    final Annotation annotation = new Annotation(text);
    nlp.annotate(annotation);
    if (Log.isDebugEnabled()) {
      for (CoreMap sentence: annotation.get(SentencesAnnotation.class)) {
        sentence.get(TokensAnnotation.class).stream().forEach(token -> Log.debug(Stream.of(
          token.get(TextAnnotation.class),
          token.get(PartOfSpeechAnnotation.class),
          token.get(LemmaAnnotation.class),
          token.get(NamedEntityTagAnnotation.class)
        ).map(comp -> "[" + comp + "]").collect(Collectors.joining(", "))));
      }
    }
  }

}
