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

import au.com.bytecode.opencsv.CSVWriter;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;
import javax.json.JsonWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author <a href="http://gregor.middell.net/">Gregor Middell</a>
 */
public class Samples {

  private final File textsFile;
  private final File catalogueFile;

  public Samples(File dir) {
    if (!dir.isDirectory() && !dir.mkdirs()) {
      throw new IllegalArgumentException(String.format("Cannot create directory %s", dir));
    }
    this.catalogueFile = new File(dir, "catalogue.tar.bz2");
    this.textsFile = new File(dir, "texts.json");
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

  protected boolean textsUpToDate() {
    return textsFile.isFile() &&
      catalogueFile.isFile() &&
      (textsFile.lastModified() >= catalogueFile.lastModified());
  }

  public static void main(String[] args) {
    try {
      final ArrayDeque<String> argDeque = new ArrayDeque<>(Arrays.asList(args));

      final Samples samples = Optional.ofNullable(argDeque.poll())
        .map(File::new).map(Samples::new)
        .orElseThrow(() -> new IllegalArgumentException(Arrays.toString(args)));

      final File out = Optional.ofNullable(argDeque.poll())
        .map(File::new)
        .orElse(null);

      final List<Text> texts = samples.texts();
      Collections.sort(texts, Comparator
        .comparing((Text txt) -> txt.language)
        .thenComparing((Text txt) -> txt.downloads, Comparator.<Integer>naturalOrder().reversed())
        .thenComparing((Text txt) -> txt.creator)
        .thenComparing((Text txt) -> txt.title)
        .thenComparing((Text txt) -> txt.resource.toString())
      );

      final Writer outWriter = out == null
        ? new PrintWriter(System.out)
        : Files.newBufferedWriter(out.toPath(), UTF_8);

      try (final CSVWriter csv = new CSVWriter(outWriter)) {
        csv.writeNext(new String[] {
          "Language",
          "Downloads",
          "Creator",
          "Title",
          "URL"
        });
        for (Text text : texts) {
          csv.writeNext(new String[] {
            text.language,
            Integer.toString(text.downloads),
            text.creator,
            text.title,
            text.resource.toString()
          });
        }

      }

      System.exit(0);
    } catch (Throwable t) {
      t.printStackTrace();
      System.exit(1);
    }
  }
}
