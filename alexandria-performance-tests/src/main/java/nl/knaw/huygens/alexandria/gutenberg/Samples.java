package nl.knaw.huygens.alexandria.gutenberg;

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

import nl.knaw.huygens.alexandria.stanfordnlp.Pipeline;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;
import javax.json.JsonWriter;
import javax.xml.XMLConstants;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.LogManager;

import static nl.knaw.huygens.alexandria.gutenberg.Text.TEI_NS_URI;

/**
 * @author <a href="http://gregor.middell.net/">Gregor Middell</a>
 */
public class Samples {

  private final File textsFile;
  private final File catalogueFile;
  private final File teiFiles;
  private final XMLOutputFactory xmlOutputFactory;

  public Samples(File dir) {

    existingDirectory(dir);
    this.catalogueFile = new File(dir, "catalogue.tar.bz2");
    this.textsFile = new File(dir, "texts.json");
    this.teiFiles = existingDirectory(new File(dir, "tei"));

    this.xmlOutputFactory = XMLOutputFactory.newFactory();
  }

  public List<Text> texts() throws IOException {
    if (textsUpToDate()) {
      try (final JsonReader reader = Json.createReader(new FileInputStream(textsFile))) {
        final List<Text> texts = new ArrayList<>();
        for (JsonValue textValue : reader.readArray()) {
          switch (textValue.getValueType()) {
            case OBJECT:
              texts.add(Text.fromJson((JsonObject) textValue));
              break;
          }
        }
        return texts;
      }
    }
    try (Catalogue catalogue = Catalogue.cached(catalogueFile)) {
      final List<Text> texts = Text.allOf(catalogue);
      try (JsonWriter writer = Json.createWriter(new FileOutputStream(textsFile))) {
        final JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
        texts.forEach(txt -> arrayBuilder.add(txt.toJson()));
        writer.writeArray(arrayBuilder.build());
      }
      return texts;
    }
  }

  public File tei(Pipeline nlp, Text text) throws IOException, XMLStreamException {
    final File teiFile = new File(teiFiles, String.format("%s.xml", text.id));
    if (teiFile.isFile() && teiFile.length() > 0) {
      return teiFile;
    }

    try (BufferedOutputStream teiStream = new BufferedOutputStream(new FileOutputStream(teiFile))) {
      final XMLStreamWriter xmlStream = xmlOutputFactory.createXMLStreamWriter(teiStream);
      try {
        writeTei(text, nlp, xmlStream);
      } finally {
        xmlStream.close();
      }
    }

    return teiFile;
  }

  protected boolean textsUpToDate() {
    return textsFile.isFile() &&
      catalogueFile.isFile() &&
      (textsFile.lastModified() >= catalogueFile.lastModified());
  }

  protected static void writeTei(Text text, Pipeline nlp, XMLStreamWriter xml) throws XMLStreamException, IOException {
    xml.writeStartDocument();
    xml.writeStartElement("", "TEI", TEI_NS_URI);
    xml.writeDefaultNamespace(TEI_NS_URI);
    xml.writeNamespace("xsi", XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI);
    xml.writeAttribute(
      "xsi", XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, "schemaLocation",
      TEI_NS_URI + " http://www.tei-c.org/release/xml/tei/custom/schema/xsd/tei_all.xsd"
    );

    text.writeTeiHeader(xml);

    xml.writeStartElement("text");
    xml.writeStartElement("body");
    xml.writeStartElement("div");
    nlp.writeAnnotated(xml, Text.body(text.contents()));
    xml.writeEndElement();
    xml.writeEndElement();
    xml.writeEndElement();

    xml.writeEndElement();
    xml.writeEndDocument();
  }

  private static File existingDirectory(File dir) {
    if (dir.isDirectory() || dir.mkdirs()) {
      return dir;
    }
    throw new IllegalArgumentException(dir.toString());
  }

  public static void main(String[] args) {
    try {
      if (System.getProperty("java.util.logging.config.file") == null) {
        try (InputStream logConfig = Samples.class.getResourceAsStream("/logging.properties")) {
          LogManager.getLogManager().readConfiguration(logConfig);
        }
      }

      final ArrayDeque<String> argDeque = new ArrayDeque<>(Arrays.asList(args));

      final Samples samples = Optional.ofNullable(argDeque.poll())
        .map(File::new).map(Samples::new)
        .orElseThrow(() -> new IllegalArgumentException("Please specify a working directory"));

      final List<Text> texts = samples.texts();
      Collections.sort(texts, Comparator
        .comparing((Text txt) -> txt.language)
        .thenComparing((Text txt) -> txt.downloads, Comparator.<Integer>naturalOrder().reversed())
        .thenComparing((Text txt) -> txt.creator)
        .thenComparing((Text txt) -> txt.title)
        .thenComparing((Text txt) -> txt.resource.toString())
      );

      final int procs = Runtime.getRuntime().availableProcessors();
      final ExecutorService threads = Executors.newFixedThreadPool((int) Math.ceil(procs / 2.0d));

      final Pipeline nlp = new Pipeline();
      texts.stream()
        .filter(text -> "en".equals(text.language)).skip(50).limit(10)
        .forEach(text -> threads.submit(() -> samples.tei(nlp, text)));

      threads.shutdown();
      threads.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
      System.exit(0);

    } catch (Throwable t) {
      t.printStackTrace();
      System.exit(1);
    }
  }
}
