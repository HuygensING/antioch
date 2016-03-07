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

import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.RDF;

import javax.json.Json;
import javax.json.JsonObject;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author <a href="http://gregor.middell.net/">Gregor Middell</a>
 */
public class Text {
  public static final String TEI_NS_URI = "http://www.tei-c.org/ns/1.0";

  private static final URI E_BOOKS_BASE_URI = URI.create("http://www.gutenberg.org/ebooks/");

  private static final Pattern HEADER_FOOTER = Pattern.compile("\\*\\*\\* ((START)|(END)) OF THIS PROJECT GUTENBERG EBOOK([^\\*])*\\*\\*\\*[\\r\\n]");

  public final String id;
  public final URI resource;
  public final String language;
  public final String creator;
  public final String title;
  public final int downloads;

  public Text(String id, URI resource, String language, String creator, String title, int downloads) {
    this.id = id;
    this.resource = resource;
    this.language = language;
    this.creator = normalizeMetadata(creator);
    this.title = normalizeMetadata(title);
    this.downloads = downloads;
  }

  public String contents() throws IOException {
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.toURL().openStream(), StandardCharsets.UTF_8))) {
      return reader.lines().collect(Collectors.joining("\n"));
    }
  }

  public static String body(String contents) {
    int start = 0;
    int end = contents.length();
    final Matcher headerFooterMatcher = HEADER_FOOTER.matcher(contents);
    while (headerFooterMatcher.find()) {
      switch (headerFooterMatcher.group(1)) {
        case "START":
          start = headerFooterMatcher.end() + 1;
          break;
        case "END":
          end = headerFooterMatcher.start() - 1;
      }
    }
    return contents.substring(start, end);
  }

  public void writeTeiHeader(XMLStreamWriter xml) throws XMLStreamException {
    xml.writeStartElement(TEI_NS_URI, "teiHeader");
    xml.writeStartElement(TEI_NS_URI, "fileDesc");

    xml.writeStartElement(TEI_NS_URI, "titleStmt");

    xml.writeStartElement(TEI_NS_URI, "title");
    xml.writeCharacters(title);
    xml.writeEndElement();
    xml.writeStartElement(TEI_NS_URI, "author");
    xml.writeCharacters(creator);
    xml.writeEndElement();

    xml.writeStartElement(TEI_NS_URI, "respStmt");
    xml.writeStartElement(TEI_NS_URI, "resp");
    xml.writeCharacters("encoded by");
    xml.writeEndElement();
    xml.writeStartElement(TEI_NS_URI, "orgName");
    xml.writeCharacters("The Alexandria Text Repository Project");
    xml.writeEndElement();
    xml.writeEndElement();

    xml.writeEndElement(); // titleStmt


    xml.writeStartElement(TEI_NS_URI, "publicationStmt");
    xml.writeStartElement(TEI_NS_URI, "distributor");
    xml.writeCharacters("Huygens ING");
    xml.writeEndElement();
    xml.writeEndElement();

    xml.writeStartElement(TEI_NS_URI, "sourceDesc");
    xml.writeStartElement(TEI_NS_URI, "bibl");
    xml.writeStartElement(TEI_NS_URI, "ref");
    xml.writeAttribute("target", resource.toString());
    xml.writeCharacters("Project Gutenberg");
    xml.writeEndElement();
    xml.writeEndElement();
    xml.writeEndElement();

    xml.writeEndElement(); // fileDesc
    xml.writeEndElement(); // teiHeader
  }
  @Override
  public String toString() {
    return Stream.of(
      String.format("%6s", id),
      "[" + resource.toString() + "]",
      "[" + language + "]",
      String.format(">%d", downloads),
      creator,
      title
    ).collect(Collectors.joining(", "));
  }

  public static Text fromJson(JsonObject json) {
    return new Text(
      json.getString("id"),
      URI.create(json.getString("uri")),
      json.getString("lang"),
      json.getString("creator"),
      json.getString("title"),
      json.getInt("downloads")
    );
  }

  public JsonObject toJson() {
    return Json.createObjectBuilder()
      .add("id", id)
      .add("uri", resource.toString())
      .add("lang", language)
      .add("creator", creator)
      .add("title", title)
      .add("downloads", downloads)
      .build();
  }

  public static String normalizeMetadata(String str) {
    return str.replaceAll("\\s+", " ");
  }

  public static List<Text> allOf(Catalogue catalogue) {
    final List<Text> texts = new ArrayList<>();
    for (Model entry : catalogue) {
      texts.addAll(allOf(entry));
    }
    return texts;
  }

  public static List<Text> allOf(Model catalogueEntry) {
    final List<Text> texts = new ArrayList<>();
    for (Iterator<Statement> langIt = catalogueEntry.listStatements(null, DCTerms.language, (RDFNode) null); langIt.hasNext(); ) {
      final Statement langStmt = langIt.next();

      final Resource book = langStmt.getSubject();
      final String id = E_BOOKS_BASE_URI.relativize(URI.create(book.getURI())).toString();
      if (!id.matches("[A-Za-z0-9]+")) {
        continue;
      }

      final RDFNode langObject = langStmt.getObject();
      if (!langObject.isResource()) {
        continue;
      }

      final String language = findFirst(catalogueEntry.listObjectsOfProperty(langObject.asResource(), RDF.value)
        .mapWith(RDFNode::asLiteral)
        .mapWith(Literal::getString))
        .orElse(null);
      if (language == null) {
        continue;
      }

      final String creator = findFirst(book
        .listProperties(DCTerms.creator)
        .mapWith(Statement::getObject).mapWith(RDFNode::asResource)
        .mapWith(c -> c.getRequiredProperty(PgTerms.name))
        .mapWith(Statement::getObject).mapWith(RDFNode::asLiteral).mapWith(Literal::getString))
        .orElse(null);
      if (creator == null) {
        continue;
      }

      final int downloads = findFirst(book.listProperties(PgTerms.downloads).mapWith(Statement::getObject)
        .mapWith(RDFNode::asLiteral).mapWith(Literal::getInt)).orElse(0);

      final String title = book.getRequiredProperty(DCTerms.title).getObject().asLiteral().getString();

      findFirst(book.listProperties(DCTerms.hasFormat)
        .mapWith(Statement::getObject)
        .mapWith(RDFNode::asResource)
        .filterKeep(text -> text.getURI().endsWith(".utf-8"))
        .filterKeep(text -> {
          final Set<String> formats = text
            .listProperties(DCTerms.format)
            .mapWith(formatStmt -> formatStmt
              .getObject().asResource().getRequiredProperty(RDF.value)
              .getObject().asLiteral().getString()
            ).toSet();

          return formats.contains("text/plain") || !formats.contains("application/zip");
        }))
        .ifPresent(text -> texts.add(new Text(id, URI.create(text.getURI()), language, creator, title, downloads)));
    }
    return texts;
  }

  protected static <T> Optional<T> findFirst(ExtendedIterator<T> iterator) {
    return iterator.hasNext() ? Optional.of(iterator.next()) : Optional.empty();
  }
}
